package com.runmarket.pacer.infrastructure.persistence;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.port.out.run.RunRepository;
import com.runmarket.pacer.infrastructure.persistence.mapper.RunMapper;
import com.runmarket.pacer.infrastructure.persistence.repository.RunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RunPersistenceAdapter implements RunRepository {

    private final RunJpaRepository runJpaRepository;
    private final RunMapper runMapper;

    @Override
    public UUID save(Run run) {
        return runJpaRepository.save(runMapper.toJpaEntity(run)).getId();
    }

    @Override
    public Optional<UUID> findIdByUserIdAndClientRunId(UUID userId, String clientRunId) {
        return runJpaRepository.findIdByUserIdAndClientRunId(userId, clientRunId);
    }

    @Override
    public List<Run> findSummariesByUserId(UUID userId) {
        return runJpaRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(runMapper::toDomainSummary)
                .toList();
    }

    @Override
    public Optional<Run> findByIdAndUserIdWithRoute(UUID runId, UUID userId) {
        return runJpaRepository.findByIdAndUserIdWithRoute(runId, userId)
                .map(runMapper::toDomainWithRoute);
    }
}
