package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.event.PasswordResetEvent;
import com.runmarket.pacer.domain.exception.InvalidPasswordResetTokenException;
import com.runmarket.pacer.domain.model.PasswordResetToken;
import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.RequestPasswordResetCommand;
import com.runmarket.pacer.domain.port.in.auth.RequestPasswordResetUseCase;
import com.runmarket.pacer.domain.port.in.auth.ResetPasswordCommand;
import com.runmarket.pacer.domain.port.in.auth.ResetPasswordUseCase;
import com.runmarket.pacer.domain.port.out.event.DomainEventPublisher;
import com.runmarket.pacer.domain.port.out.security.PasswordHashPort;
import com.runmarket.pacer.domain.port.out.user.PasswordResetTokenRepository;
import com.runmarket.pacer.domain.port.out.user.UserRepository;
import com.runmarket.pacer.domain.port.out.verification.CaptchaVerificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements RequestPasswordResetUseCase, ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final DomainEventPublisher eventPublisher;
    private final PasswordHashPort passwordHashPort;
    private final CaptchaVerificationPort captchaVerificationPort;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.verification.token-expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Override
    @Transactional
    public void requestReset(RequestPasswordResetCommand command) {
        captchaVerificationPort.verify(command.captchaToken());

        Optional<User> user = userRepository.findByEmail(command.email());
        if (user.isEmpty()) {
            // 이메일 열거 공격 방지: 존재하지 않는 이메일이어도 동일하게 성공 응답을 반환한다.
            log.info("Password reset requested for non-existent email");
            return;
        }

        UUID userId = user.get().getId();
        tokenRepository.deleteByUserId(userId);

        String rawToken = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .userId(userId)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .build());

        eventPublisher.publish(new PasswordResetEvent(
                command.email(), baseUrl + "/reset-password?token=" + rawToken));
        log.info("Password reset requested: userId={}", userId);
    }

    @Override
    @Transactional
    public void reset(ResetPasswordCommand command) {
        PasswordResetToken resetToken = tokenRepository.findByToken(command.token())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.isExpired()) {
            throw new InvalidPasswordResetTokenException();
        }

        userRepository.updatePassword(resetToken.getUserId(), passwordHashPort.encode(command.newPassword()));
        tokenRepository.deleteByUserId(resetToken.getUserId());
        log.info("Password reset completed: userId={}", resetToken.getUserId());
    }
}
