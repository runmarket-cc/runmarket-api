package com.runmarket.pacer.infrastructure.persistence;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.port.out.run.RunRepository;
import com.runmarket.pacer.infrastructure.persistence.mapper.RunMapper;
import com.runmarket.pacer.infrastructure.persistence.repository.RunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
