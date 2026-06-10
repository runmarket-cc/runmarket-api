package com.runmarket.pacer.socket.pubsub;

import com.runmarket.pacer.socket.session.SessionRegistry;
import com.runmarket.pacer.socket.session.SinkRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;


@Slf4j
@Component
public class RedisMessageRelay {

    private static final String RUNNER_CHANNEL_PREFIX = "runner:";
    private static final String GROUP_CHANNEL_PREFIX = "group:";

    private final SessionRegistry sessionRegistry;
    private final SinkRegistry sinkRegistry;
    private final ReactiveRedisMessageListenerContainer listenerContainer;
    private Disposable subscription;

    public RedisMessageRelay(
            ReactiveRedisConnectionFactory connectionFactory,
            SessionRegistry sessionRegistry,
            SinkRegistry sinkRegistry) {
        this.sessionRegistry = sessionRegistry;
        this.sinkRegistry = sinkRegistry;
        this.listenerContainer = new ReactiveRedisMessageListenerContainer(connectionFactory);
    }

    @PostConstruct
    public void start() {
        subscription = listenerContainer
                .receive(PatternTopic.of(RUNNER_CHANNEL_PREFIX + "*"), PatternTopic.of(GROUP_CHANNEL_PREFIX + "*"))
                .flatMap(msg -> relay(msg.getChannel(), msg.getMessage()))
                .onErrorContinue((e, o) -> log.warn("Redis relay error: {}", e.getMessage()))
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    private Mono<Void> relay(String channel, String payload) {
        if (channel.startsWith(GROUP_CHANNEL_PREFIX)) {
            String groupId = channel.substring(GROUP_CHANNEL_PREFIX.length());
            return relayToSessions(sessionRegistry.getGroupSessions(groupId), payload);
        }
        String runnerId = channel.substring(RUNNER_CHANNEL_PREFIX.length());
        return relayToSessions(sessionRegistry.getSessions(runnerId), payload);
    }

    private Mono<Void> relayToSessions(Iterable<WebSocketSession> sessions, String payload) {
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> {
                    WebSocketMessage msg = session.textMessage(payload);
                    Sinks.Many<WebSocketMessage> sink = sinkRegistry.getSink(session.getId()).orElse(null);
                    if (sink != null) {
                        sink.tryEmitNext(msg);
                        return Mono.empty();
                    }
                    log.warn("No sink for session {}, falling back to direct send", session.getId());
                    return session.send(Mono.just(msg))
                            .onErrorResume(e -> {
                                log.warn("Send failed for session {}: {}", session.getId(), e.getMessage());
                                return Mono.empty();
                            });
                })
                .then();
    }
}
