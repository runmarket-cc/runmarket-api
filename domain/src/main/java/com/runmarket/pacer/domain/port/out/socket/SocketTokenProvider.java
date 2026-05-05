package com.runmarket.pacer.domain.port.out.socket;

import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.AuthToken;

public interface SocketTokenProvider {
    AuthToken generateRunnerToken(User user, String groupId, String runnerId);
    AuthToken generateSpectatorToken(User user, String groupId);
}
