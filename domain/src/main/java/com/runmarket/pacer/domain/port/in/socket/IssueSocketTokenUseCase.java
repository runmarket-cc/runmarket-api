package com.runmarket.pacer.domain.port.in.socket;

import com.runmarket.pacer.domain.port.in.auth.AuthToken;

public interface IssueSocketTokenUseCase {
    AuthToken issue(IssueSocketTokenCommand command);
}
