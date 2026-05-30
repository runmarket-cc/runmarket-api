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

    @Test
    void registerGroup_sessionIsRetrievable() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.registerGroup("AAAA", session);

        assertThat(registry.getGroupSessions("AAAA")).containsExactly(session);
    }

    @Test
    void registerGroup_multipleSpectatorsForSameGroup() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.registerGroup("AAAA", s1);
        registry.registerGroup("AAAA", s2);

        assertThat(registry.getGroupSessions("AAAA")).containsExactlyInAnyOrder(s1, s2);
    }

    @Test
    void unregisterGroup_removesSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.registerGroup("AAAA", session);
        registry.unregisterGroup("AAAA", session);

        assertThat(registry.getGroupSessions("AAAA")).isEmpty();
    }

    @Test
    void unregisterGroup_oneOfMultiple_othersRemain() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.registerGroup("AAAA", s1);
        registry.registerGroup("AAAA", s2);
        registry.unregisterGroup("AAAA", s1);

        assertThat(registry.getGroupSessions("AAAA")).containsExactly(s2);
    }

    @Test
    void getGroupSessions_unknownGroup_returnsEmptySet() {
        assertThat(registry.getGroupSessions("UNKNOWN")).isEmpty();
    }

    @Test
    void registerGroup_differentGroups_isolated() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        registry.registerGroup("AAAA", s1);
        registry.registerGroup("BBBB", s2);

        assertThat(registry.getGroupSessions("AAAA")).containsExactly(s1);
        assertThat(registry.getGroupSessions("BBBB")).containsExactly(s2);
    }

    @Test
    void runnerSessions_and_groupSessions_areIndependent() {
        WebSocketSession runnerSession = mock(WebSocketSession.class);
        WebSocketSession groupSession = mock(WebSocketSession.class);
        registry.register("runner-1", runnerSession);
        registry.registerGroup("AAAA", groupSession);

        assertThat(registry.getSessions("runner-1")).containsExactly(runnerSession);
        assertThat(registry.getGroupSessions("AAAA")).containsExactly(groupSession);
        assertThat(registry.getSessions("AAAA")).isEmpty();
        assertThat(registry.getGroupSessions("runner-1")).isEmpty();
    }
}
