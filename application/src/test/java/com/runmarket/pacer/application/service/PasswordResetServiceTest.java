package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.event.PasswordResetEvent;
import com.runmarket.pacer.domain.exception.InvalidPasswordResetTokenException;
import com.runmarket.pacer.domain.model.PasswordResetToken;
import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.RequestPasswordResetCommand;
import com.runmarket.pacer.domain.port.in.auth.ResetPasswordCommand;
import com.runmarket.pacer.domain.port.out.event.DomainEventPublisher;
import com.runmarket.pacer.domain.port.out.security.PasswordHashPort;
import com.runmarket.pacer.domain.port.out.user.PasswordResetTokenRepository;
import com.runmarket.pacer.domain.port.out.user.UserRepository;
import com.runmarket.pacer.domain.port.out.verification.CaptchaVerificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private PasswordHashPort passwordHashPort;
    @Mock
    private CaptchaVerificationPort captchaVerificationPort;

    @InjectMocks
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "https://www.runmarket.cc");
        ReflectionTestUtils.setField(service, "tokenExpiryMinutes", 30);
    }

    @Test
    void requestReset_existingUser_savesTokenAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmail("user@runmarket.cc"))
                .thenReturn(Optional.of(User.builder().id(userId).email("user@runmarket.cc").build()));

        service.requestReset(new RequestPasswordResetCommand("user@runmarket.cc", "captcha-token"));

        verify(captchaVerificationPort).verify("captcha-token");
        verify(tokenRepository).deleteByUserId(userId);
        verify(tokenRepository).save(any(PasswordResetToken.class));

        ArgumentCaptor<PasswordResetEvent> eventCaptor = ArgumentCaptor.forClass(PasswordResetEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        PasswordResetEvent event = eventCaptor.getValue();
        assertThat(event.email()).isEqualTo("user@runmarket.cc");
        assertThat(event.resetLink()).startsWith("https://www.runmarket.cc/reset-password?token=");
    }

    @Test
    void requestReset_nonExistentEmail_returnsSilentlyWithoutTokenOrEvent() {
        when(userRepository.findByEmail("ghost@runmarket.cc")).thenReturn(Optional.empty());

        service.requestReset(new RequestPasswordResetCommand("ghost@runmarket.cc", "captcha-token"));

        // 이메일 열거 공격 방지: 검증은 수행하되 토큰 생성/메일 발송은 일어나지 않는다.
        verify(captchaVerificationPort).verify("captcha-token");
        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void reset_validToken_updatesPasswordAndDeletesToken() {
        UUID userId = UUID.randomUUID();
        when(tokenRepository.findByToken("raw-token"))
                .thenReturn(Optional.of(PasswordResetToken.builder()
                        .userId(userId)
                        .token("raw-token")
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .build()));
        when(passwordHashPort.encode("newPassword123")).thenReturn("encoded-password");

        service.reset(new ResetPasswordCommand("raw-token", "newPassword123"));

        verify(userRepository).updatePassword(userId, "encoded-password");
        verify(tokenRepository).deleteByUserId(userId);
    }

    @Test
    void reset_unknownToken_throwsInvalidPasswordResetToken() {
        when(tokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reset(new ResetPasswordCommand("missing", "newPassword123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(userRepository, never()).updatePassword(any(), any());
    }

    @Test
    void reset_expiredToken_throwsInvalidPasswordResetToken() {
        when(tokenRepository.findByToken("expired"))
                .thenReturn(Optional.of(PasswordResetToken.builder()
                        .userId(UUID.randomUUID())
                        .token("expired")
                        .expiresAt(LocalDateTime.now().minusMinutes(1))
                        .build()));

        assertThatThrownBy(() -> service.reset(new ResetPasswordCommand("expired", "newPassword123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(userRepository, never()).updatePassword(any(), any());
    }
}
