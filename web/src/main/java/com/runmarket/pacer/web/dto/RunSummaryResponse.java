package com.runmarket.pacer.web.dto;

import com.runmarket.pacer.domain.model.Run;

import java.time.Instant;
import java.time.ZoneOffset;

/** 러닝 기록 목록 항목(궤적 제외). 시각은 UTC ISO-8601 로 직렬화된다. */
public record RunSummaryResponse(
        String id,
        Instant startedAt,
        Instant endedAt,
        int durationSec,
        double distanceKm,
        int avgPaceSecPerKm,
        String runnerId,
        String groupId,
        String color
) {
    public static RunSummaryResponse from(Run run) {
        return new RunSummaryResponse(
                run.getId().toString(),
                run.getStartedAt().toInstant(ZoneOffset.UTC),
                run.getEndedAt().toInstant(ZoneOffset.UTC),
                run.getDurationSec(),
                run.getDistanceKm(),
                run.getAvgPaceSecPerKm(),
                run.getRunnerId(),
                run.getGroupId(),
                run.getColor()
        );
    }
}
