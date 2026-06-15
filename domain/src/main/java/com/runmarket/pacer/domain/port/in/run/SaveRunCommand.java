package com.runmarket.pacer.domain.port.in.run;

import java.time.Instant;
import java.util.List;

public record SaveRunCommand(
        String userEmail,
        String clientRunId,
        String groupId,
        String runnerId,
        Instant startedAt,
        Instant endedAt,
        int durationSec,
        double distanceKm,
        int avgPaceSecPerKm,
        String color,
        List<RoutePoint> route
) {
    /** 궤적 한 점. t 는 epoch milliseconds. */
    public record RoutePoint(double lat, double lng, long t, Double accuracy) {}
}
