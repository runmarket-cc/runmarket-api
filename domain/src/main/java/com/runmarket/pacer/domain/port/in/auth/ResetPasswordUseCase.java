package com.runmarket.pacer.domain.port.in.auth;

public interface ResetPasswordUseCase {
    void reset(ResetPasswordCommand command);
}
