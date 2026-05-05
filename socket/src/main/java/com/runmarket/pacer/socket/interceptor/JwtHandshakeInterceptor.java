package com.runmarket.pacer.socket.interceptor;

import com.runmarket.pacer.socket.exception.JwtAuthException;
import com.runmarket.pacer.socket.model.WsRole;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JwtHandshakeInterceptor {

    private final SecretKey secretKey;

    public JwtHandshakeInterceptor(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<WsSessionAttributes> validate(WebSocketSession session) {
        return Mono.fromCallable(() -> {
            String token = extractToken(session);
            Claims claims = parseClaims(token);
            return toSessionAttributes(claims);
        });
    }

    private String extractToken(WebSocketSession session) {
        String token = UriComponentsBuilder
                .fromUri(session.getHandshakeInfo().getUri())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (StringUtil.isNullOrEmpty(token)) {
            throw new JwtAuthException("Missing token");
        }
        return token;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new JwtAuthException("Invalid token: " + e.getMessage());
        }
    }

    private WsSessionAttributes toSessionAttributes(Claims claims) {
        String wsRoleStr = claims.get("wsRole", String.class);
        if (wsRoleStr == null) throw new JwtAuthException("Missing wsRole claim");

        WsRole role;
        try {
            role = WsRole.valueOf(wsRoleStr);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthException("Unknown wsRole: " + wsRoleStr);
        }

        String raceId = claims.get("raceId", String.class);
        if (raceId == null) throw new JwtAuthException("Missing raceId claim");

        if (role == WsRole.RUNNER) {
            String groupId = claims.get("groupId", String.class);
            String runnerId = claims.get("runnerId", String.class);
            if (groupId == null) throw new JwtAuthException("Missing groupId claim");
            if (runnerId == null) throw new JwtAuthException("Missing runnerId claim");
            return new WsSessionAttributes(claims.getSubject(), raceId, role, runnerId, Set.of(groupId));
        } else {
            List<String> groupIds = claims.get("groupIds", List.class);
            if (groupIds == null || groupIds.isEmpty()) throw new JwtAuthException("Missing groupIds claim");
            return new WsSessionAttributes(claims.getSubject(), raceId, role, null, new HashSet<>(groupIds));
        }
    }
}
