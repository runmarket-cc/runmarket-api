package com.runmarket.pacer.socket;

import com.redis.testcontainers.RedisContainer;
import io.jsonwebtoken.Jwts;
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
