package com.runmarket.pacer.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 사용자의 마라톤 러닝 1건 기록.
 *
 * 앱은 러닝 궤적을 기기에 적재한 뒤 종료 시 업로드한다(local-first).
 * clientRunId 는 기기/런 단위 고유값으로, (userId, clientRunId) 조합으로 멱등 처리된다.
 */
@Getter
@Builder
public class Run {
    private UUID id;
    private UUID userId;
    private String clientRunId;
    private String groupId;
    private String runnerId;
    private String color;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int durationSec;
    private double distanceKm;
    private int avgPaceSecPerKm;
    private List<RunPoint> route;
    private LocalDateTime createdAt;
}
