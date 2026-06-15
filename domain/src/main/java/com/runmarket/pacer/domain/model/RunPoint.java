package com.runmarket.pacer.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 러닝 궤적의 한 점 (GPS 좌표 + 정확도 + 기록 시각). */
@Getter
@Builder
public class RunPoint {
    private int seq;
    private double lat;
    private double lng;
    private Double accuracy; // GPS 수평 정확도(m), 없으면 null
    private LocalDateTime recordedAt;
}
