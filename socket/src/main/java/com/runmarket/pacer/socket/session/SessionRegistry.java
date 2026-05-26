package com.runmarket.pacer.socket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(String runnerId, WebSocketSession session) {
        sessions.computeIfAbsent(runnerId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(String runnerId, WebSocketSession session) {
        sessions.computeIfPresent(runnerId, (k, set) -> {
            set.remove(session);
            return set.isEmpty() ? null : set;
        });
    }

    public Set<WebSocketSession> getSessions(String runnerId) {
        return sessions.getOrDefault(runnerId, Set.of());
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }
}
