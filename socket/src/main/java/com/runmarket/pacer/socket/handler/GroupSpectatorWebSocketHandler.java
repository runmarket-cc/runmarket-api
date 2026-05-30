package com.runmarket.pacer.socket.handler;

import com.runmarket.pacer.socket.model.WsRole;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import com.runmarket.pacer.socket.security.WsAuthenticationToken;
import com.runmarket.pacer.socket.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupSpectatorWebSocketHandler implements WebSocketHandler {

    private final SessionRegistry sessionRegistry;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.getHandshakeInfo().getPrincipal()
                .cast(WsAuthenticationToken.class)
                .map(WsAuthenticationToken::getAttributes)
                .flatMap(attrs -> {
                    if (attrs.role() != WsRole.SPECTATOR) {
                        log.warn("Non-spectator attempted group subscription: role={}", attrs.role());
                        return session.close(CloseStatus.POLICY_VIOLATION);
                    }
                    String groupId = extractGroupId(session);
                    if (!groupId.equals(attrs.groupId())) {
                        log.warn("Group mismatch: path={}, jwt={}", groupId, attrs.groupId());
                        return session.close(CloseStatus.POLICY_VIOLATION);
                    }
                    return handleGroupSpectator(session, groupId);
                })
                .onErrorResume(e -> {
                    if (e instanceof AbortedException) {
                        return Mono.empty();
                    }
                    log.error("Group spectator handler error [{}]: {}", session.getId(), e.getMessage());
                    return session.close(CloseStatus.SERVER_ERROR)
                            .onErrorResume(ignored -> Mono.empty());
                });
    }

    private Mono<Void> handleGroupSpectator(WebSocketSession session, String groupId) {
        return Mono.usingWhen(
                Mono.fromCallable(() -> {
                    sessionRegistry.registerGroup(groupId, session);
                    log.info("Group spectator connected: groupId={}", groupId);
                    return session;
                }),
                s -> s.receive().then(),
                s -> {
                    sessionRegistry.unregisterGroup(groupId, s);
                    log.info("Group spectator disconnected: groupId={}", groupId);
                    return Mono.empty();
                }
        );
    }

    private String extractGroupId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
