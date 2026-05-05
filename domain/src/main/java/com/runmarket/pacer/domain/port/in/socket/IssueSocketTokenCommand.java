package com.runmarket.pacer.domain.port.in.socket;

import java.util.List;
import java.util.UUID;

public record IssueSocketTokenCommand(
        String userEmail,
        UUID raceId,
        String role,
        String groupId,      // RUNNER only
        String runnerId,     // RUNNER only
        List<String> groupIds // SPECTATOR only
) {}
