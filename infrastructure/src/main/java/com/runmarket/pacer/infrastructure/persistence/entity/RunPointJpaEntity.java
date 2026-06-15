package com.runmarket.pacer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 러닝 궤적의 한 점. 한 러닝에 수천 개가 쌓이므로 감사(audit) 컬럼 없는 경량 엔티티로 둔다.
 */
@Entity
@Table(name = "run_points")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunPointJpaEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false, updatable = false)
    private RunJpaEntity run;

    @Column(nullable = false, updatable = false)
    private int seq;

    @Column(nullable = false, updatable = false)
    private double lat;

    @Column(nullable = false, updatable = false)
    private double lng;

    @Column(updatable = false)
    private Double accuracy;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    void assignRun(RunJpaEntity run) {
        this.run = run;
    }
}
