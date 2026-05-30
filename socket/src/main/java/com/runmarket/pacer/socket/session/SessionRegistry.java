package com.runmarket.pacer.socket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<WebSocketSession>> groupSessions = new ConcurrentHashMap<>();

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

    public void registerGroup(String groupId, WebSocketSession session) {
        groupSessions.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregisterGroup(String groupId, WebSocketSession session) {
        groupSessions.computeIfPresent(groupId, (k, set) -> {
            set.remove(session);
            return set.isEmpty() ? null : set;
        });
    }

    public Set<WebSocketSession> getGroupSessions(String groupId) {
        return groupSessions.getOrDefault(groupId, Set.of());
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }
}
