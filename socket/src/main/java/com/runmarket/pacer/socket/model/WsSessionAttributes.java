package com.runmarket.pacer.socket.model;

public record WsSessionAttributes(
        String email,
        WsRole role,
        String groupId,
        String runnerId  // RUNNER only, null for SPECTATOR
) {}
