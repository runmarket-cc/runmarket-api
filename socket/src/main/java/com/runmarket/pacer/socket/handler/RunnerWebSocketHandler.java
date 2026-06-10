package com.runmarket.pacer.socket.handler;

import com.runmarket.pacer.socket.model.WsSessionAttributes;
import com.runmarket.pacer.socket.security.WsAuthenticationToken;
import com.runmarket.pacer.socket.session.SessionRegistry;
import com.runmarket.pacer.socket.session.SinkRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.channel.AbortedException;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerWebSocketHandler implements WebSocketHandler {

    private static final String GROUP_KEY_PREFIX = "runner:group:";
    private static final String SESSION_KEY_PREFIX = "runner:session:";
    private static final String GROUP_MEMBERS_KEY_PREFIX = "runner:members:";
    private static final String CHANNEL_PREFIX = "runner:";
    private static final String GROUP_CHANNEL_PREFIX = "group:";
    private static final Duration PING_INTERVAL = Duration.ofSeconds(20);

    private final SessionRegistry sessionRegistry;
    private final SinkRegistry sinkRegistry;
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

        Sinks.Many<WebSocketMessage> sink = Sinks.many().unicast().onBackpressureBuffer();

        Mono<Long> setup = redisTemplate.opsForValue().set(groupKey, attrs.groupId())
                .then(redisTemplate.opsForValue().set(sessionKey, sessionId))
                .then(redisTemplate.opsForSet().add(membersKey, runnerId))
                .doOnSuccess(v -> {
                    sinkRegistry.register(sessionId, sink);
                    sessionRegistry.register(runnerId, session);
                    sessionRegistry.registerGroup(attrs.groupId(), session);
                    log.info("Runner connected: runnerId={}, group={}", runnerId, attrs.groupId());
                });

        Mono<Void> redisCleanup = redisTemplate.opsForValue().get(sessionKey)
                .filter(sessionId::equals)
                .flatMap(ignored -> redisTemplate.opsForSet().remove(membersKey, (Object) runnerId)
                        .then(redisTemplate.delete(sessionKey))
                        .then(redisTemplate.delete(groupKey)))
                .then();

        return setup.flatMap(v -> {
            Disposable ping = Flux.interval(PING_INTERVAL)
                    .subscribe(i -> sink.tryEmitNext(
                            session.pingMessage(f -> f.wrap(new byte[0]))));

            Mono<Void> receive = session.receive()
                    .flatMap(msg -> {
                        String payload = msg.getPayloadAsText();
                        String groupMsg = "{\"runnerId\":\"" + runnerId + "\",\"data\":" + payload + "}";
                        return redisTemplate.convertAndSend(CHANNEL_PREFIX + runnerId, payload)
                                .then(redisTemplate.convertAndSend(GROUP_CHANNEL_PREFIX + attrs.groupId(), groupMsg));
                    })
                    .doFinally(signal -> {
                        ping.dispose();
                        sink.tryEmitComplete();
                        sinkRegistry.unregister(sessionId);
                        sessionRegistry.unregister(runnerId, session);
                        sessionRegistry.unregisterGroup(attrs.groupId(), session);
                        log.info("Runner disconnected: runnerId={}", runnerId);
                    })
                    .then()
                    .then(redisCleanup);

            return Mono.zip(session.send(sink.asFlux()), receive).then();
        });
    }

    private Mono<Void> handleSpectator(WebSocketSession session, WsSessionAttributes attrs, String runnerId) {
        return redisTemplate.opsForValue().get(GROUP_KEY_PREFIX + runnerId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Runner not found: " + runnerId)))
                .flatMap(runnerGroupId -> {
                    if (!attrs.groupId().equals(runnerGroupId)) {
                        log.warn("Group mismatch: spectator={}, runner={}", attrs.groupId(), runnerGroupId);
                        return session.close(CloseStatus.POLICY_VIOLATION);
                    }

                    String sessionId = session.getId();
                    Sinks.Many<WebSocketMessage> sink = Sinks.many().unicast().onBackpressureBuffer();

                    sinkRegistry.register(sessionId, sink);
                    sessionRegistry.register(runnerId, session);
                    log.info("Spectator connected: runnerId={}, group={}", runnerId, attrs.groupId());

                    Disposable ping = Flux.interval(PING_INTERVAL)
                            .subscribe(i -> sink.tryEmitNext(
                                    session.pingMessage(f -> f.wrap(new byte[0]))));

                    Mono<Void> receive = session.receive()
                            .doFinally(signal -> {
                                ping.dispose();
                                sink.tryEmitComplete();
                                sinkRegistry.unregister(sessionId);
                                sessionRegistry.unregister(runnerId, session);
                                log.info("Spectator disconnected from runner={}", runnerId);
                            })
                            .then();

                    return Mono.zip(session.send(sink.asFlux()), receive).then();
                });
    }

    private String extractRunnerId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
