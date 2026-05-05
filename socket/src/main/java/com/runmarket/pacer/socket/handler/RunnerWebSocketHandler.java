package com.runmarket.pacer.socket.handler;

import com.runmarket.pacer.socket.exception.JwtAuthException;
import com.runmarket.pacer.socket.interceptor.JwtHandshakeInterceptor;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import com.runmarket.pacer.socket.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerWebSocketHandler implements WebSocketHandler {

    private static final String GROUP_KEY_PREFIX = "runner:group:";
    private static final String CHANNEL_PREFIX = "runner:";

    private final JwtHandshakeInterceptor jwtInterceptor;
    private final SessionRegistry sessionRegistry;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return jwtInterceptor.validate(session)
                .flatMap(attrs -> {
                    String runnerId = extractRunnerId(session);
                    return switch (attrs.role()) {
                        case RUNNER -> handleRunner(session, attrs, runnerId);
                        case SPECTATOR -> handleSpectator(session, attrs, runnerId);
                    };
                })
                .onErrorResume(JwtAuthException.class, e -> {
                    log.warn("Auth failed [{}]: {}", session.getId(), e.getMessage());
                    return session.close(CloseStatus.POLICY_VIOLATION);
                })
                .onErrorResume(e -> {
                    log.error("Handler error [{}]: {}", session.getId(), e.getMessage());
                    return session.close(CloseStatus.SERVER_ERROR);
                });
    }

    private Mono<Void> handleRunner(WebSocketSession session, WsSessionAttributes attrs, String runnerId) {
        if (!runnerId.equals(attrs.runnerId())) {
            log.warn("runnerId mismatch: path={}, jwt={}", runnerId, attrs.runnerId());
            return session.close(CloseStatus.POLICY_VIOLATION);
        }

        String groupKey = GROUP_KEY_PREFIX + runnerId;

        Mono<Boolean> setup = redisTemplate.opsForValue().set(groupKey, attrs.groupId())
                .doOnSuccess(v -> {
                    sessionRegistry.register(runnerId, session);
                    log.info("Runner connected: runnerId={}, group={}", runnerId, attrs.groupId());
                });

        return Mono.usingWhen(
                setup,
                v -> session.receive()
                        .flatMap(msg -> redisTemplate.convertAndSend(CHANNEL_PREFIX + runnerId, msg.getPayloadAsText()))
                        .then(),
                v -> {
                    sessionRegistry.unregister(runnerId, session);
                    log.info("Runner disconnected: runnerId={}", runnerId);
                    return redisTemplate.delete(groupKey).then();
                }
        );
    }

    private Mono<Void> handleSpectator(WebSocketSession session, WsSessionAttributes attrs, String runnerId) {
        return redisTemplate.opsForValue().get(GROUP_KEY_PREFIX + runnerId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Runner not active: " + runnerId)))
                .flatMap(runnerGroupId -> {
                    if (!attrs.groupId().equals(runnerGroupId)) {
                        log.warn("Group mismatch: spectator={}, runner={}", attrs.groupId(), runnerGroupId);
                        return session.close(CloseStatus.POLICY_VIOLATION);
                    }
                    return Mono.usingWhen(
                            Mono.fromCallable(() -> {
                                sessionRegistry.register(runnerId, session);
                                log.info("Spectator connected: runnerId={}, group={}", runnerId, attrs.groupId());
                                return session;
                            }),
                            s -> s.receive().then(),
                            s -> {
                                sessionRegistry.unregister(runnerId, s);
                                log.info("Spectator disconnected from runner={}", runnerId);
                                return Mono.empty();
                            }
                    );
                });
    }

    private String extractRunnerId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
