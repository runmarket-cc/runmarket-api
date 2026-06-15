package com.runmarket.pacer.infrastructure.persistence.repository;

import com.runmarket.pacer.infrastructure.persistence.entity.RunJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RunJpaRepository extends JpaRepository<RunJpaEntity, UUID> {

    @Query("SELECT r.id FROM RunJpaEntity r WHERE r.userId = :userId AND r.clientRunId = :clientRunId")
    Optional<UUID> findIdByUserIdAndClientRunId(@Param("userId") UUID userId,
                                                @Param("clientRunId") String clientRunId);
}
