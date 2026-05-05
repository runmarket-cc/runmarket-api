package com.runmarket.pacer.web.security;

import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.AuthToken;
import com.runmarket.pacer.domain.port.out.auth.TokenProvider;
import com.runmarket.pacer.domain.port.out.socket.SocketTokenProvider;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider implements TokenProvider, SocketTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public AuthToken generateToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleType().name())
                .toList();

        return build(Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", roles));
    }

    @Override
    public AuthToken generateRunnerToken(User user, String groupId, String runnerId) {
        return build(Jwts.builder()
                .subject(user.getEmail())
                .claim("wsRole", "RUNNER")
                .claim("groupId", groupId)
                .claim("runnerId", runnerId));
    }

    @Override
    public AuthToken generateSpectatorToken(User user, String groupId) {
        return build(Jwts.builder()
                .subject(user.getEmail())
                .claim("wsRole", "SPECTATOR")
                .claim("groupId", groupId));
    }

    private AuthToken build(JwtBuilder builder) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expiry.getTime()), ZoneId.systemDefault());

        String token = builder
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();

        return new AuthToken(token, expiresAt);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
