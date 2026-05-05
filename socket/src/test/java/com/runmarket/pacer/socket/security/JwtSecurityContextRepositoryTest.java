package com.runmarket.pacer.socket.security;

import com.runmarket.pacer.socket.model.WsRole;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSecurityContextRepositoryTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes!!";

    private JwtSecurityContextRepository repository;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        repository = new JwtSecurityContextRepository(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void load_validRunnerToken_returnsSecurityContext() {
        String token = runnerToken("runner-1", "AAAA");
        MockServerWebExchange exchange = exchangeWithToken(token);

        StepVerifier.create(repository.load(exchange))
                .assertNext(ctx -> {
                    WsAuthenticationToken auth = (WsAuthenticationToken) ctx.getAuthentication();
                    WsSessionAttributes attrs = auth.getAttributes();
                    assertThat(attrs.role()).isEqualTo(WsRole.RUNNER);
                    assertThat(attrs.groupId()).isEqualTo("AAAA");
                    assertThat(attrs.runnerId()).isEqualTo("runner-1");
                    assertThat(auth.isAuthenticated()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void load_validSpectatorToken_returnsSecurityContext() {
        String token = spectatorToken("AAAA");
        MockServerWebExchange exchange = exchangeWithToken(token);

        StepVerifier.create(repository.load(exchange))
                .assertNext(ctx -> {
                    WsAuthenticationToken auth = (WsAuthenticationToken) ctx.getAuthentication();
                    WsSessionAttributes attrs = auth.getAttributes();
                    assertThat(attrs.role()).isEqualTo(WsRole.SPECTATOR);
                    assertThat(attrs.groupId()).isEqualTo("AAAA");
                    assertThat(attrs.runnerId()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void load_invalidToken_returnsEmpty() {
        MockServerWebExchange exchange = exchangeWithToken("invalid.token.value");

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();
    }

    @Test
    void load_noToken_returnsEmpty() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/runner/abc").build());

        StepVerifier.create(repository.load(exchange))
                .verifyComplete();
    }

    @Test
    void load_expiredToken_returnsEmpty() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("wsRole", "RUNNER")
                .claim("groupId", "AAAA")
                .claim("runnerId", "runner-1")
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey)
                .compact();

        StepVerifier.create(repository.load(exchangeWithToken(token)))
                .verifyComplete();
    }

    @Test
    void load_missingGroupId_returnsEmpty() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("wsRole", "RUNNER")
                .claim("runnerId", "runner-1")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();

        StepVerifier.create(repository.load(exchangeWithToken(token)))
                .verifyComplete();
    }

    @Test
    void save_alwaysReturnsEmpty() {
        StepVerifier.create(repository.save(null, null))
                .verifyComplete();
    }

    private String runnerToken(String runnerId, String groupId) {
        return Jwts.builder()
                .subject("runner@example.com")
                .claim("wsRole", "RUNNER")
                .claim("groupId", groupId)
                .claim("runnerId", runnerId)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }

    private String spectatorToken(String groupId) {
        return Jwts.builder()
                .subject("spectator@example.com")
                .claim("wsRole", "SPECTATOR")
                .claim("groupId", groupId)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }

    private MockServerWebExchange exchangeWithToken(String token) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/runner/abc?token=" + token).build());
    }
}
