package com.runmarket.pacer.domain.port.in.run;

import java.util.UUID;

public interface SaveRunUseCase {
    /**
     * 러닝 기록을 저장하고 서버 측 runId 를 반환한다.
     * 같은 (사용자, clientRunId) 의 재업로드는 새 레코드를 만들지 않고 기존 id 를 반환한다(멱등).
     */
    UUID save(SaveRunCommand command);
}
