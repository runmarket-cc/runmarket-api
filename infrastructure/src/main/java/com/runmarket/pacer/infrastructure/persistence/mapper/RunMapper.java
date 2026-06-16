package com.runmarket.pacer.infrastructure.persistence.mapper;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.model.RunPoint;
import com.runmarket.pacer.infrastructure.persistence.entity.RunJpaEntity;
import com.runmarket.pacer.infrastructure.persistence.entity.RunPointJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RunMapper {

    public RunJpaEntity toJpaEntity(Run run) {
        RunJpaEntity entity = RunJpaEntity.builder()
                .userId(run.getUserId())
                .clientRunId(run.getClientRunId())
                .groupId(run.getGroupId())
                .runnerId(run.getRunnerId())
                .color(run.getColor())
                .startedAt(run.getStartedAt())
                .endedAt(run.getEndedAt())
                .durationSec(run.getDurationSec())
                .distanceKm(run.getDistanceKm())
                .avgPaceSecPerKm(run.getAvgPaceSecPerKm())
                .build();

        if (run.getRoute() != null) {
            run.getRoute().forEach(p -> entity.addPoint(RunPointJpaEntity.builder()
                    .seq(p.getSeq())
                    .lat(p.getLat())
                    .lng(p.getLng())
                    .accuracy(p.getAccuracy())
                    .recordedAt(p.getRecordedAt())
                    .build()));
        }
        return entity;
    }

    /** 요약 도메인 변환: 궤적은 제외(빈 목록). LAZY 컬렉션을 건드리지 않는다. */
    public Run toDomainSummary(RunJpaEntity entity) {
        return baseBuilder(entity).route(List.of()).build();
    }

    /** 상세 도메인 변환: 궤적 포함. */
    public Run toDomainWithRoute(RunJpaEntity entity) {
        List<RunPoint> route = entity.getPoints().stream()
                .map(p -> RunPoint.builder()
                        .seq(p.getSeq())
                        .lat(p.getLat())
                        .lng(p.getLng())
                        .accuracy(p.getAccuracy())
                        .recordedAt(p.getRecordedAt())
                        .build())
                .toList();
        return baseBuilder(entity).route(route).build();
    }

    private Run.RunBuilder baseBuilder(RunJpaEntity entity) {
        return Run.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .clientRunId(entity.getClientRunId())
                .groupId(entity.getGroupId())
                .runnerId(entity.getRunnerId())
                .color(entity.getColor())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .durationSec(entity.getDurationSec())
                .distanceKm(entity.getDistanceKm())
                .avgPaceSecPerKm(entity.getAvgPaceSecPerKm())
                .createdAt(entity.getCreatedAt());
    }
}
