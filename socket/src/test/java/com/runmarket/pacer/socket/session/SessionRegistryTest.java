package com.runmarket.pacer.socket.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SessionRegistryTest {

    private SessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SessionRegistry();
    }

    @Test
    void register_sessionIsRetrievable() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.register("runner-1", session);

        assertThat(registry.getSessions("runner-1")).containsExactly(session);
    }

    @Test
    void register_multipleSessionsForSameRunner() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.register("runner-1", s1);
        registry.register("runner-1", s2);

        assertThat(registry.getSessions("runner-1")).containsExactlyInAnyOrder(s1, s2);
    }

    @Test
    void unregister_removesSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.register("runner-1", session);
        registry.unregister("runner-1", session);

        assertThat(registry.getSessions("runner-1")).isEmpty();
    }

    @Test
    void unregister_lastSession_removesKeyEntirely() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.register("runner-1", session);
        registry.unregister("runner-1", session);

        // 빈 Set이 아니라 key 자체가 사라져야 함
        assertThat(registry.getSessions("runner-1")).isEmpty();
        // 다시 register 후 unregister 해도 동일하게 동작
        registry.register("runner-1", session);
        registry.unregister("runner-1", session);
        assertThat(registry.getSessions("runner-1")).isEmpty();
    }

    @Test
    void unregister_oneOfMultipleSessions_othersRemain() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.register("runner-1", s1);
        registry.register("runner-1", s2);
        registry.unregister("runner-1", s1);

        assertThat(registry.getSessions("runner-1")).containsExactly(s2);
    }

    @Test
    void getSessions_unknownRunner_returnsEmptySet() {
        assertThat(registry.getSessions("unknown")).isEmpty();
    }

    @Test
    void register_differentRunners_isolated() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.register("runner-1", s1);
        registry.register("runner-2", s2);

        assertThat(registry.getSessions("runner-1")).containsExactly(s1);
        assertThat(registry.getSessions("runner-2")).containsExactly(s2);
    }
}
