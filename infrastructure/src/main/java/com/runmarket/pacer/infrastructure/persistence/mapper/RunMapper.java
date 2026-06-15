package com.runmarket.pacer.infrastructure.persistence.mapper;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.infrastructure.persistence.entity.RunJpaEntity;
import com.runmarket.pacer.infrastructure.persistence.entity.RunPointJpaEntity;
import org.springframework.stereotype.Component;

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
}
