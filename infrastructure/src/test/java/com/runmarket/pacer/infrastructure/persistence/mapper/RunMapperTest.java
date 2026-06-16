package com.runmarket.pacer.infrastructure.persistence.mapper;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.model.RunPoint;
import com.runmarket.pacer.infrastructure.persistence.entity.RunJpaEntity;
import com.runmarket.pacer.infrastructure.persistence.entity.RunPointJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RunMapperTest {

    private final RunMapper mapper = new RunMapper();

    private Run sampleRun(List<RunPoint> route) {
        return Run.builder()
                .userId(UUID.randomUUID())
                .clientRunId("alice-1000")
                .groupId("GROUP1")
                .runnerId("alice")
                .color("#ff9900")
                .startedAt(LocalDateTime.of(2026, 6, 15, 0, 0, 0))
                .endedAt(LocalDateTime.of(2026, 6, 15, 0, 2, 0))
                .durationSec(120)
                .distanceKm(0.42)
                .avgPaceSecPerKm(286)
                .route(route)
                .build();
    }

    private RunPoint point(int seq, Double accuracy) {
        return RunPoint.builder()
                .seq(seq)
                .lat(37.5665 + seq * 0.001)
                .lng(126.9780 + seq * 0.001)
                .accuracy(accuracy)
                .recordedAt(LocalDateTime.of(2026, 6, 15, 0, 0, seq * 3))
                .build();
    }

    @Test
    void toJpaEntity_mapsScalarFields() {
        Run run = sampleRun(List.of(point(0, 5.0)));

        RunJpaEntity entity = mapper.toJpaEntity(run);

        assertThat(entity.getUserId()).isEqualTo(run.getUserId());
        assertThat(entity.getClientRunId()).isEqualTo("alice-1000");
        assertThat(entity.getGroupId()).isEqualTo("GROUP1");
        assertThat(entity.getRunnerId()).isEqualTo("alice");
        assertThat(entity.getColor()).isEqualTo("#ff9900");
        assertThat(entity.getStartedAt()).isEqualTo(run.getStartedAt());
        assertThat(entity.getEndedAt()).isEqualTo(run.getEndedAt());
        assertThat(entity.getDurationSec()).isEqualTo(120);
        assertThat(entity.getDistanceKm()).isEqualTo(0.42);
        assertThat(entity.getAvgPaceSecPerKm()).isEqualTo(286);
    }

    @Test
    void toJpaEntity_mapsRoutePointsInOrderWithBackReference() {
        Run run = sampleRun(List.of(point(0, 5.0), point(1, null), point(2, 8.5)));

        RunJpaEntity entity = mapper.toJpaEntity(run);

        assertThat(entity.getPoints()).hasSize(3);
        assertThat(entity.getPoints()).extracting(RunPointJpaEntity::getSeq).containsExactly(0, 1, 2);
        assertThat(entity.getPoints()).extracting(RunPointJpaEntity::getAccuracy).containsExactly(5.0, null, 8.5);
        // 모든 점이 부모 run 을 역참조해야 cascade insert 시 FK 가 채워진다
        assertThat(entity.getPoints()).allSatisfy(p -> assertThat(p.getRun()).isSameAs(entity));
    }

    @Test
    void toJpaEntity_emptyRoute_yieldsNoPoints() {
        Run run = sampleRun(List.of());

        RunJpaEntity entity = mapper.toJpaEntity(run);

        assertThat(entity.getPoints()).isEmpty();
    }

    @Test
    void toJpaEntity_nullRoute_yieldsNoPoints() {
        Run run = sampleRun(null);

        RunJpaEntity entity = mapper.toJpaEntity(run);

        assertThat(entity.getPoints()).isEmpty();
    }
}
