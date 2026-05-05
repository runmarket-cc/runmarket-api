package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.AuthToken;
import com.runmarket.pacer.domain.port.in.socket.IssueSocketTokenCommand;
import com.runmarket.pacer.domain.port.in.socket.IssueSocketTokenUseCase;
import com.runmarket.pacer.domain.port.out.socket.SocketTokenProvider;
import com.runmarket.pacer.domain.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocketTokenService implements IssueSocketTokenUseCase {

    private final UserRepository userRepository;
    private final SocketTokenProvider socketTokenProvider;

    @Override
    public AuthToken issue(IssueSocketTokenCommand command) {
        User user = userRepository.findByEmail(command.userEmail())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        return switch (command.role()) {
            case "RUNNER" -> socketTokenProvider.generateRunnerToken(user, command.groupId(), command.runnerId());
            case "SPECTATOR" -> socketTokenProvider.generateSpectatorToken(user, command.groupId());
            default -> throw new IllegalArgumentException("올바르지 않은 role입니다: " + command.role());
        };
    }
}
