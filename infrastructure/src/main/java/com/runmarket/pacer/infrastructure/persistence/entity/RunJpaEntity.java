package com.runmarket.pacer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "runs",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_runs_user_client_run",
                columnNames = {"user_id", "client_run_id"})
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunJpaEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "client_run_id", nullable = false, updatable = false)
    private String clientRunId;

    @Column(name = "group_id", nullable = false, updatable = false)
    private String groupId;

    @Column(name = "runner_id", nullable = false, updatable = false)
    private String runnerId;

    @Column(updatable = false)
    private String color;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false, updatable = false)
    private LocalDateTime endedAt;

    @Column(name = "duration_sec", nullable = false, updatable = false)
    private int durationSec;

    @Column(name = "distance_km", nullable = false, updatable = false)
    private double distanceKm;

    @Column(name = "avg_pace_sec_per_km", nullable = false, updatable = false)
    private int avgPaceSecPerKm;

    @Builder.Default
    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq ASC")
    private List<RunPointJpaEntity> points = new ArrayList<>();

    /** 양방향 연관 설정과 함께 궤적 점을 추가한다. */
    public void addPoint(RunPointJpaEntity point) {
        point.assignRun(this);
        this.points.add(point);
    }
}
