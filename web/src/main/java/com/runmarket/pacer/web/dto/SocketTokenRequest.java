package com.runmarket.pacer.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record SocketTokenRequest(
        @NotBlank String role,
        @NotBlank String groupId,
        String runnerId
) {
    @AssertTrue(message = "RUNNER는 runnerId가 필요합니다.")
    private boolean isRunnerFieldsValid() {
        if (!"RUNNER".equals(role)) return true;
        return runnerId != null && !runnerId.isBlank();
    }
}
