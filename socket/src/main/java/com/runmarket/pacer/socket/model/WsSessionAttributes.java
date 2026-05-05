package com.runmarket.pacer.socket.model;

import java.util.Set;

public record WsSessionAttributes(
        String email,
        String raceId,
        WsRole role,
        String runnerId,       // RUNNER only, null for SPECTATOR
        Set<String> groupIds   // RUNNER: singleton {"A"}, SPECTATOR: {"A","B",...}
) {}
