package com.runmarket.pacer.socket.security;

import com.runmarket.pacer.socket.exception.JwtAuthException;
import com.runmarket.pacer.socket.model.WsRole;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final SecretKey secretKey;

    public JwtSecurityContextRepository(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) return Mono.empty();

        return Mono.fromCallable(() -> {
            WsSessionAttributes attrs = parseToken(token);
            return (SecurityContext) new SecurityContextImpl(new WsAuthenticationToken(attrs));
        }).onErrorResume(JwtAuthException.class, e -> {
            log.warn("JWT validation failed: {}", e.getMessage());
            return Mono.empty();
        });
    }

    private WsSessionAttributes parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new JwtAuthException("Invalid token: " + e.getMessage());
        }

        String wsRoleStr = claims.get("wsRole", String.class);
        if (wsRoleStr == null) throw new JwtAuthException("Missing wsRole claim");

        WsRole role;
        try {
            role = WsRole.valueOf(wsRoleStr);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthException("Unknown wsRole: " + wsRoleStr);
        }

        String groupId = claims.get("groupId", String.class);
        if (groupId == null) throw new JwtAuthException("Missing groupId claim");

        String runnerId = claims.get("runnerId", String.class);
        if (role == WsRole.RUNNER && runnerId == null) throw new JwtAuthException("Missing runnerId claim");

        return new WsSessionAttributes(claims.getSubject(), role, groupId, runnerId);
    }
}
