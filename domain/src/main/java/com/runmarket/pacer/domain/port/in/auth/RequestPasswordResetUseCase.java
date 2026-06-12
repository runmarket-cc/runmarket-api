package com.runmarket.pacer.domain.port.in.auth;

public interface RequestPasswordResetUseCase {
    void requestReset(RequestPasswordResetCommand command);
}
