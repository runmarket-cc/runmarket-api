package com.runmarket.pacer.web.dto;

import com.runmarket.pacer.domain.model.Run;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/** 러닝 기록 상세(궤적 포함). */
public record RunDetailResponse(
        String id,
        Instant startedAt,
        Instant endedAt,
        int durationSec,
        double distanceKm,
        int avgPaceSecPerKm,
        String runnerId,
        String groupId,
        String color,
        List<RunRoutePointResponse> route
) {
    public static RunDetailResponse from(Run run) {
        List<RunRoutePointResponse> route = run.getRoute().stream()
                .map(RunRoutePointResponse::from)
                .toList();
        return new RunDetailResponse(
                run.getId().toString(),
                run.getStartedAt().atZone(ZoneId.systemDefault()).toInstant(),
                run.getEndedAt().atZone(ZoneId.systemDefault()).toInstant(),
                run.getDurationSec(),
                run.getDistanceKm(),
                run.getAvgPaceSecPerKm(),
                run.getRunnerId(),
                run.getGroupId(),
                run.getColor(),
                route
        );
    }
}
