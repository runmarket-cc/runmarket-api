package com.runmarket.pacer.web.dto;

import com.runmarket.pacer.domain.model.RunPoint;

import java.time.ZoneId;

/** 궤적 한 점. t 는 epoch milliseconds, acc 는 GPS 정확도(m, nullable). */
public record RunRoutePointResponse(double lat, double lng, long t, Double acc) {
    public static RunRoutePointResponse from(RunPoint p) {
        return new RunRoutePointResponse(
                p.getLat(),
                p.getLng(),
                p.getRecordedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                p.getAccuracy()
        );
    }
}
