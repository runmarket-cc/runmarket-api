package com.runmarket.pacer.domain.port.out.run;

import com.runmarket.pacer.domain.model.Run;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunRepository {
    /** 러닝 기록을 저장하고 생성된 id 를 반환한다. */
    UUID save(Run run);

    /** 멱등 처리용: 이미 업로드된 (사용자, clientRunId) 의 기존 runId 조회. */
    Optional<UUID> findIdByUserIdAndClientRunId(UUID userId, String clientRunId);

    /** 사용자의 러닝 기록 요약 목록(궤적 제외)을 최신순으로 조회. */
    List<Run> findSummariesByUserId(UUID userId);

    /** 사용자의 러닝 기록 1건을 궤적 포함하여 조회. */
    Optional<Run> findByIdAndUserIdWithRoute(UUID runId, UUID userId);
}
