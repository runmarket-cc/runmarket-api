package com.runmarket.pacer.domain.port.out.run;

import com.runmarket.pacer.domain.model.Run;

import java.util.Optional;
import java.util.UUID;

public interface RunRepository {
    /** 러닝 기록을 저장하고 생성된 id 를 반환한다. */
    UUID save(Run run);

    /** 멱등 처리용: 이미 업로드된 (사용자, clientRunId) 의 기존 runId 조회. */
    Optional<UUID> findIdByUserIdAndClientRunId(UUID userId, String clientRunId);
}
