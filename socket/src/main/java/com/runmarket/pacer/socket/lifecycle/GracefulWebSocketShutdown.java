package com.runmarket.pacer.socket.lifecycle;

import com.runmarket.pacer.socket.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class GracefulWebSocketShutdown implements SmartLifecycle {

    private final SessionRegistry sessionRegistry;
    private volatile boolean running = true;

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;

        Collection<?> all = sessionRegistry.getAllSessions();
        if (all.isEmpty()) {
            log.info("Graceful WebSocket shutdown: no active sessions");
            return;
        }

        log.info("Graceful WebSocket shutdown: sending SERVICE_RESTARTED to {} sessions", all.size());

        // 모든 세션에 SERVICE_RESTARTED(1012) close frame 전송
        // 클라이언트는 이 코드를 받으면 즉시 재접속 시도
        Flux.fromIterable(sessionRegistry.getAllSessions())
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.close(CloseStatus.SERVICE_RESTARTED)
                        .onErrorResume(e -> {
                            log.debug("Session {} already closed", session.getId());
                            return Mono.empty();
                        }))
                .blockLast(Duration.ofSeconds(10));

        log.info("Graceful WebSocket shutdown complete");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * phase가 높을수록 먼저 종료됨.
     * Netty 서버(Integer.MAX_VALUE - 1)보다 먼저 종료되어야
     * 채널이 닫히기 전에 close frame을 전송할 수 있음.
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
