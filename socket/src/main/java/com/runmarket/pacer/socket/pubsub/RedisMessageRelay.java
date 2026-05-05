package com.runmarket.pacer.socket.pubsub;

import com.runmarket.pacer.socket.session.SessionRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class RedisMessageRelay {

    private static final String CHANNEL_PREFIX = "runner:";

    private final SessionRegistry sessionRegistry;
    private final ReactiveRedisMessageListenerContainer listenerContainer;
    private Disposable subscription;

    public RedisMessageRelay(
            ReactiveRedisConnectionFactory connectionFactory,
            SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
        this.listenerContainer = new ReactiveRedisMessageListenerContainer(connectionFactory);
    }

    @PostConstruct
    public void start() {
        subscription = listenerContainer
                .receive(PatternTopic.of(CHANNEL_PREFIX + "*"))
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
        String runnerId = channel.substring(CHANNEL_PREFIX.length());
        return Flux.fromIterable(sessionRegistry.getSessions(runnerId))
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(payload)))
                        .onErrorResume(e -> {
                            log.warn("Send failed for session {}: {}", session.getId(), e.getMessage());
                            return Mono.empty();
                        }))
                .then();
    }
}
