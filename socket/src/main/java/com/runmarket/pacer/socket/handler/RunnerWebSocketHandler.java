package com.runmarket.pacer.socket.handler;

import com.runmarket.pacer.socket.model.WsSessionAttributes;
import com.runmarket.pacer.socket.security.WsAuthenticationToken;
import com.runmarket.pacer.socket.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerWebSocketHandler implements WebSocketHandler {

    private static final String GROUP_KEY_PREFIX = "runner:group:";
    private static final String SESSION_KEY_PREFIX = "runner:session:";
    private static final String GROUP_MEMBERS_KEY_PREFIX = "runner:members:";
    private static final String CHANNEL_PREFIX = "runner:";
    private static final String GROUP_CHANNEL_PREFIX = "group:";

    private final SessionRegistry sessionRegistry;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.getHandshakeInfo().getPrincipal()
                .cast(WsAuthenticationToken.class)
                .map(WsAuthenticationToken::getAttributes)
                .flatMap(attrs -> {
                    String runnerId = extractRunnerId(session);
                    return switch (attrs.role()) {
                        case RUNNER -> handleRunner(session, attrs, runnerId);
                        case SPECTATOR -> handleSpectator(session, attrs, runnerId);
                    };
                })
                .onErrorResume(e -> {
                    if (e instanceof AbortedException) {
                        return Mono.empty();
                    }
                    log.error("Handler error [{}]: {}", session.getId(), e.getMessage());
                    return session.close(CloseStatus.SERVER_ERROR)
                            .onErrorResume(ignored -> Mono.empty());
                });
    }

    private Mono<Void> handleRunner(WebSocketSession session, WsSessionAttributes attrs, String runnerId) {
        if (!runnerId.equals(attrs.runnerId())) {
            log.warn("runnerId mismatch: path={}, jwt={}", runnerId, attrs.runnerId());
            return session.close(CloseStatus.POLICY_VIOLATION);
        }

        String groupKey = GROUP_KEY_PREFIX + runnerId;
        String sessionKey = SESSION_KEY_PREFIX + runnerId;
        String membersKey = GROUP_MEMBERS_KEY_PREFIX + attrs.groupId();
        String sessionId = session.getId();

        Mono<Long> setup = redisTemplate.opsForValue().set(groupKey, attrs.groupId())
                .then(redisTemplate.opsForValue().set(sessionKey, sessionId))
                .then(redisTemplate.opsForSet().add(membersKey, runnerId))
                .doOnSuccess(v -> {
                    sessionRegistry.register(runnerId, session);
                    log.info("Runner connected: runnerId={}, group={}", runnerId, attrs.groupId());
                });

        return Mono.usingWhen(
                setup,
                v -> session.receive()
                        .flatMap(msg -> {
                            String payload = msg.getPayloadAsText();
                            String groupMsg = "{\"runnerId\":\"" + runnerId + "\",\"data\":" + payload + "}";
                            return redisTemplate.convertAndSend(CHANNEL_PREFIX + runnerId, payload)
                                    .then(redisTemplate.convertAndSend(GROUP_CHANNEL_PREFIX + attrs.groupId(), groupMsg));
                        })
                        .then(),
                v -> {
                    sessionRegistry.unregister(runnerId, session);
                    log.info("Runner disconnected: runnerId={}", runnerId);
                    // 재연결 race condition 방지: 새 연결이 이미 Redis를 덮어썼다면 정리 생략
                    return redisTemplate.opsForValue().get(sessionKey)
                            .filter(sessionId::equals)
                            .flatMap(ignored -> redisTemplate.opsForSet().remove(membersKey, (Object) runnerId)
                                    .then(redisTemplate.delete(sessionKey))
                                    .then(redisTemplate.delete(groupKey)))
                            .then();
                }
        );
    }

    private Mono<Void> handleSpectator(WebSocketSession session, WsSessionAttributes attrs, String runnerId) {
        return redisTemplate.opsForValue().get(GROUP_KEY_PREFIX + runnerId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Runner not found: " + runnerId)))
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
