package com.runmarket.pacer.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SocketTokenRequest(
        @NotBlank String role,
        @NotBlank String groupId,
        @NotBlank String runnerId,
        @NotEmpty List<String> groupIds
) {}
