package com.runmarket.pacer.domain.port.in.socket;

public record IssueSocketTokenCommand(
        String userEmail,
        String role,
        String groupId,
        String runnerId  // RUNNER only
) {}
