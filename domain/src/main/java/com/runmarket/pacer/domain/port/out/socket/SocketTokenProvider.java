package com.runmarket.pacer.domain.port.out.socket;

import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.auth.AuthToken;

import java.util.List;

public interface SocketTokenProvider {
    AuthToken generateRunnerToken(User user, String raceId, String groupId, String runnerId);
    AuthToken generateSpectatorToken(User user, String raceId, List<String> groupIds);
}
