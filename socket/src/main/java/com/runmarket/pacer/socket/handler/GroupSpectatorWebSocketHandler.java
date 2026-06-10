package com.runmarket.pacer.socket.handler;

import com.runmarket.pacer.socket.model.WsRole;
import com.runmarket.pacer.socket.model.WsSessionAttributes;
import com.runmarket.pacer.socket.security.WsAuthenticationToken;
import com.runmarket.pacer.socket.session.SessionRegistry;
import com.runmarket.pacer.socket.session.SinkRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class GroupSpectatorWebSocketHandler implements WebSocketHandler {

    private static final Duration PING_INTERVAL = Duration.ofSeconds(20);

    private final SessionRegistry sessionRegistry;
    private final SinkRegistry sinkRegistry;

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
        String sessionId = session.getId();
        Sinks.Many<WebSocketMessage> sink = Sinks.many().unicast().onBackpressureBuffer();

        sinkRegistry.register(sessionId, sink);
        sessionRegistry.registerGroup(groupId, session);
        log.info("Group spectator connected: groupId={}", groupId);

        Disposable ping = Flux.interval(PING_INTERVAL)
                .subscribe(i -> sink.tryEmitNext(
                        session.pingMessage(f -> f.wrap(new byte[0]))));

        Mono<Void> receive = session.receive()
                .doFinally(signal -> {
                    ping.dispose();
                    sink.tryEmitComplete();
                    sinkRegistry.unregister(sessionId);
                    sessionRegistry.unregisterGroup(groupId, session);
                    log.info("Group spectator disconnected: groupId={}", groupId);
                })
                .then();

        return Mono.zip(session.send(sink.asFlux()), receive).then();
    }

    private String extractGroupId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
