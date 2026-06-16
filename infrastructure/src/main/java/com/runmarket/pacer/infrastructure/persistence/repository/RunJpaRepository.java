package com.runmarket.pacer.infrastructure.persistence.repository;

import com.runmarket.pacer.infrastructure.persistence.entity.RunJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunJpaRepository extends JpaRepository<RunJpaEntity, UUID> {

    @Query("SELECT r.id FROM RunJpaEntity r WHERE r.userId = :userId AND r.clientRunId = :clientRunId")
    Optional<UUID> findIdByUserIdAndClientRunId(@Param("userId") UUID userId,
                                                @Param("clientRunId") String clientRunId);

    /** 요약 목록: 궤적(points)은 로드하지 않는다(LAZY). 최신순. */
    List<RunJpaEntity> findByUserIdOrderByStartedAtDesc(UUID userId);

    /** 상세: 궤적을 fetch join 으로 함께 로드. */
    @Query("SELECT r FROM RunJpaEntity r LEFT JOIN FETCH r.points WHERE r.id = :id AND r.userId = :userId")
    Optional<RunJpaEntity> findByIdAndUserIdWithRoute(@Param("id") UUID id, @Param("userId") UUID userId);
}
