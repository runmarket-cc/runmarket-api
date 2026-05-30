package com.runmarket.pacer.socket;

import com.redis.testcontainers.RedisContainer;
import io.jsonwebtoken.Jwts;
import static org.assertj.core.api.Assertions.assertThat;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("7"));

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @LocalServerPort
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

    @Test
    void runner_connects_and_sends_location_spectator_receives() {
        String runnerId = "runner-test-1";
        String groupId = "AAAA";

        String runnerToken = runnerToken(runnerId, groupId);
        String spectatorToken = spectatorToken(groupId);

        URI runnerUri = uri("/ws/runner/" + runnerId, runnerToken);
        URI spectatorUri = uri("/ws/runner/" + runnerId, spectatorToken);

        String payload = "{\"lat\":37.5,\"lng\":127.0}";

        Sinks.One<String> received = Sinks.one();

        // 1. 러너 먼저 연결 (백그라운드) - Redis 등록 후 800ms 뒤에 메시지 전송
        client.execute(runnerUri, session ->
                session.send(
                        Mono.delay(Duration.ofMillis(800))
                                .thenReturn(session.textMessage(payload))
                ).then(Mono.delay(Duration.ofSeconds(3))).then()
        ).subscribe();

        // 2. 러너가 Redis에 그룹 키를 등록할 때까지 대기
        Mono.delay(Duration.ofMillis(400)).block();

        // 3. 관전자 연결 (백그라운드)
        client.execute(spectatorUri, session ->
                session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .next()
                        .doOnNext(received::tryEmitValue)
                        .then()
        ).subscribe();

        // 4. 관전자가 메시지 수신 확인
        StepVerifier.create(received.asMono().timeout(Duration.ofSeconds(5)))
                .expectNext(payload)
                .verifyComplete();
    }

    @Test
    void invalid_token_returns_401() {
        URI uri = uri("/ws/runner/abc", "invalid.token");

        // HTTP 401이 반환되어 WebSocket 연결 자체가 거부됨
        StepVerifier.create(
                        client.execute(uri, session -> Mono.empty())
                )
                .expectError()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void spectator_different_group_is_rejected() {
        String runnerId = "runner-test-2";
        String runnerToken = runnerToken(runnerId, "AAAA");
        String spectatorToken = spectatorToken("BBBB");  // 다른 그룹

        URI runnerUri = uri("/ws/runner/" + runnerId, runnerToken);
        URI spectatorUri = uri("/ws/runner/" + runnerId, spectatorToken);

        // 1. 러너 먼저 연결 (그룹 등록)
        client.execute(runnerUri, session ->
                Mono.delay(Duration.ofSeconds(3)).then()
        ).subscribe();

        // 2. 러너가 Redis에 등록될 때까지 대기
        Mono.delay(Duration.ofMillis(400)).block();

        // 3. 다른 그룹 관전자는 POLICY_VIOLATION close frame 수신 후 정상 종료
        StepVerifier.create(
                        client.execute(spectatorUri, session ->
                                session.receive().then()
                        )
                )
                .verifyComplete();
    }

    @Test
    void multiple_runners_in_group_group_spectator_receives_all_with_runnerId() {
        String groupId = "MULTI-GROUP";
        String runner1Id = "runner-multi-1";
        String runner2Id = "runner-multi-2";

        String runner1Token = runnerToken(runner1Id, groupId);
        String runner2Token = runnerToken(runner2Id, groupId);
        String spectatorToken = spectatorToken(groupId);

        URI runner1Uri = uri("/ws/runner/" + runner1Id, runner1Token);
        URI runner2Uri = uri("/ws/runner/" + runner2Id, runner2Token);
        URI groupSpectatorUri = uri("/ws/group/" + groupId, spectatorToken);

        String payload1 = "{\"lat\":37.1,\"lng\":127.1}";
        String payload2 = "{\"lat\":37.2,\"lng\":127.2}";
        String expectedMsg1 = "{\"runnerId\":\"" + runner1Id + "\",\"data\":" + payload1 + "}";
        String expectedMsg2 = "{\"runnerId\":\"" + runner2Id + "\",\"data\":" + payload2 + "}";

        Sinks.Many<String> received = Sinks.many().replay().all();

        // 1. 두 러너 먼저 연결
        client.execute(runner1Uri, session ->
                session.send(Mono.delay(Duration.ofMillis(800)).thenReturn(session.textMessage(payload1)))
                        .then(Mono.delay(Duration.ofSeconds(4))).then()
        ).subscribe();

        client.execute(runner2Uri, session ->
                session.send(Mono.delay(Duration.ofMillis(1000)).thenReturn(session.textMessage(payload2)))
                        .then(Mono.delay(Duration.ofSeconds(4))).then()
        ).subscribe();

        // 2. 러너들이 Redis에 등록될 때까지 대기
        Mono.delay(Duration.ofMillis(400)).block();

        // 3. 그룹 관전자 연결
        client.execute(groupSpectatorUri, session ->
                session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .take(2)
                        .doOnNext(received::tryEmitNext)
                        .then()
        ).subscribe();

        // 4. 두 메시지 모두 수신 확인 (순서 무관)
        StepVerifier.create(
                        received.asFlux().take(2).collectList().timeout(Duration.ofSeconds(6))
                )
                .assertNext(messages -> {
                    assertThat(messages).hasSize(2);
                    assertThat(messages).containsExactlyInAnyOrder(expectedMsg1, expectedMsg2);
                })
                .verifyComplete();
    }

    @Test
    void group_spectator_receives_only_its_group_messages() {
        String groupA = "GROUP-A";
        String groupB = "GROUP-B";
        String runnerAId = "runner-group-a";
        String runnerBId = "runner-group-b";

        client.execute(uri("/ws/runner/" + runnerAId, runnerToken(runnerAId, groupA)), session ->
                session.send(Mono.delay(Duration.ofMillis(800)).thenReturn(session.textMessage("{\"group\":\"A\"}")))
                        .then(Mono.delay(Duration.ofSeconds(4))).then()
        ).subscribe();

        client.execute(uri("/ws/runner/" + runnerBId, runnerToken(runnerBId, groupB)), session ->
                session.send(Mono.delay(Duration.ofMillis(800)).thenReturn(session.textMessage("{\"group\":\"B\"}")))
                        .then(Mono.delay(Duration.ofSeconds(4))).then()
        ).subscribe();

        Mono.delay(Duration.ofMillis(400)).block();

        Sinks.One<String> received = Sinks.one();

        // GROUP-A 관전자는 GROUP-A 러너 메시지만 수신
        client.execute(uri("/ws/group/" + groupA, spectatorToken(groupA)), session ->
                session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .next()
                        .doOnNext(received::tryEmitValue)
                        .then()
        ).subscribe();

        StepVerifier.create(received.asMono().timeout(Duration.ofSeconds(5)))
                .assertNext(msg -> assertThat(msg).contains("\"runnerId\":\"" + runnerAId + "\""))
                .verifyComplete();
    }

    @Test
    void runner_role_cannot_connect_to_group_endpoint() {
        String runnerId = "runner-group-reject";
        String groupId = "REJECT-GROUP";
        String runnerToken = runnerToken(runnerId, groupId);

        // RUNNER 토큰으로 /ws/group/ 접근 시 POLICY_VIOLATION close frame 후 정상 종료
        StepVerifier.create(
                        client.execute(uri("/ws/group/" + groupId, runnerToken), session ->
                                session.receive().then()
                        )
                )
                .verifyComplete();
    }

    private URI uri(String path, String token) {
        return URI.create("ws://localhost:" + port + path + "?token=" + token);
    }

    private String runnerToken(String runnerId, String groupId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("runner@example.com")
                .claim("wsRole", "RUNNER")
                .claim("groupId", groupId)
                .claim("runnerId", runnerId)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    private String spectatorToken(String groupId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("spectator@example.com")
                .claim("wsRole", "SPECTATOR")
                .claim("groupId", groupId)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }
}
