package com.runmarket.pacer.socket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SinkRegistry {

    private final ConcurrentHashMap<String, Sinks.Many<WebSocketMessage>> sinks = new ConcurrentHashMap<>();

    public void register(String sessionId, Sinks.Many<WebSocketMessage> sink) {
        sinks.put(sessionId, sink);
    }

    public void unregister(String sessionId) {
        sinks.remove(sessionId);
    }

    public Optional<Sinks.Many<WebSocketMessage>> getSink(String sessionId) {
        return Optional.ofNullable(sinks.get(sessionId));
    }
}
