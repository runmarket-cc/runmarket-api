package com.runmarket.pacer.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * 러닝 기록 업로드 요청 (POST /api/v1/runs).
 * 앱의 RunUploadPayload 와 1:1 대응한다. startedAt/endedAt 은 ISO-8601 문자열.
 */
public record SaveRunRequest(
        @NotBlank String clientRunId,
        @NotBlank String groupId,
        @NotBlank String runnerId,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt,
        @NotNull Integer durationSec,
        @NotNull Double distanceKm,
        @NotNull Integer avgPaceSecPerKm,
        String color,
        @NotNull List<RunRoutePointRequest> route
) {}
