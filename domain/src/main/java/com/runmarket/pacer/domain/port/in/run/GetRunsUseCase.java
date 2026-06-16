package com.runmarket.pacer.domain.port.in.run;

import com.runmarket.pacer.domain.model.Run;

import java.util.List;
import java.util.UUID;

public interface GetRunsUseCase {
    /** 사용자의 러닝 기록 목록(요약, 궤적 제외)을 최신순으로 반환한다. */
    List<Run> getUserRuns(String userEmail);

    /** 사용자의 러닝 기록 1건(궤적 포함)을 반환한다. 없으면 예외. */
    Run getUserRun(String userEmail, UUID runId);
}
