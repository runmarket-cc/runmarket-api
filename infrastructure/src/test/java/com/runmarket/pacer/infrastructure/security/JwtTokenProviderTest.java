package com.runmarket.pacer.infrastructure.security;

import com.runmarket.pacer.domain.model.Role;
import com.runmarket.pacer.domain.model.RoleType;
import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.AuthToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "runmarket-secret-key-must-be-at-least-512-bits-for-hs512-algorithm!!!";
    private static final long EXPIRATION_30_DAYS = 2592000000L; // 30 days
    private static final long THIRTY_MINUTES_MS = 30 * 60 * 1000L;

    private JwtTokenProvider tokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_30_DAYS);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private User sampleUser(String email) {
        return User.builder()
                .email(email)
                .roles(List.of(Role.builder().roleType(RoleType.ROLE_USER).build()))
                .build();
    }

    private String createTokenWithRemainingTime(String email, long remainingMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + remainingMs);

        return Jwts.builder()
                .subject(email)
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date(now.getTime() - 10000))
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Test
    @DisplayName("토큰 생성 시 만료기간이 약 30일 후로 설정된다")
    void generateToken_setsExpirationTo30Days() {
        User user = sampleUser("runner@example.com");

        AuthToken authToken = tokenProvider.generateToken(user);

        assertThat(authToken.token()).isNotBlank();
        assertThat(authToken.expiresAt()).isAfter(LocalDateTime.now().plusDays(29));
        assertThat(ChronoUnit.DAYS.between(LocalDateTime.now(), authToken.expiresAt())).isBetween(29L, 31L);
        assertThat(tokenProvider.validateToken(authToken.token())).isTrue();
        assertThat(tokenProvider.getSubject(authToken.token())).isEqualTo("runner@example.com");
    }

    @Test
    @DisplayName("만료 시간이 30분 이하로 남은 경우 isExpiringWithin은 true를 반환한다")
    void isExpiringWithin_returnsTrueWhenRemainingTimeUnder30Minutes() {
        // 10분 남은 토큰
        String token = createTokenWithRemainingTime("user@example.com", 10 * 60 * 1000L);

        boolean expiringSoon = tokenProvider.isExpiringWithin(token, THIRTY_MINUTES_MS);

        assertThat(expiringSoon).isTrue();
    }

    @Test
    @DisplayName("만료 시간이 30분을 초과하여 남은 경우 isExpiringWithin은 false를 반환한다")
    void isExpiringWithin_returnsFalseWhenRemainingTimeOver30Minutes() {
        // 1시간 남은 토큰
        String token = createTokenWithRemainingTime("user@example.com", 60 * 60 * 1000L);

        boolean expiringSoon = tokenProvider.isExpiringWithin(token, THIRTY_MINUTES_MS);

        assertThat(expiringSoon).isFalse();
    }

    @Test
    @DisplayName("이미 만료된 토큰의 경우 isExpiringWithin은 false를 반환한다")
    void isExpiringWithin_returnsFalseForExpiredToken() {
        // 이미 만료된 토큰 (-10분)
        String expiredToken = createTokenWithRemainingTime("user@example.com", -10 * 60 * 1000L);

        boolean expiringSoon = tokenProvider.isExpiringWithin(expiredToken, THIRTY_MINUTES_MS);

        assertThat(expiringSoon).isFalse();
    }

    @Test
    @DisplayName("refreshToken은 기존 subject와 roles 클레임을 유지하며 갱신된 30일 만료 토큰을 발급한다")
    void refreshToken_renewsTokenWithClaimsAndRefreshedExpiry() {
        String currentToken = createTokenWithRemainingTime("renew@example.com", 15 * 60 * 1000L);

        AuthToken refreshed = tokenProvider.refreshToken(currentToken);

        assertThat(refreshed.token()).isNotBlank();
        assertThat(tokenProvider.validateToken(refreshed.token())).isTrue();
        assertThat(tokenProvider.getSubject(refreshed.token())).isEqualTo("renew@example.com");
        assertThat(ChronoUnit.DAYS.between(LocalDateTime.now(), refreshed.expiresAt())).isBetween(29L, 31L);

        // 갱신된 토큰은 이제 30분 이내 만료 대상이 아니어야 함
        assertThat(tokenProvider.isExpiringWithin(refreshed.token(), THIRTY_MINUTES_MS)).isFalse();
    }
}
