package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.model.RunPoint;
import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.run.GetRunsUseCase;
import com.runmarket.pacer.domain.port.in.run.SaveRunCommand;
import com.runmarket.pacer.domain.port.in.run.SaveRunUseCase;
import com.runmarket.pacer.domain.port.out.run.RunRepository;
import com.runmarket.pacer.domain.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RunService implements SaveRunUseCase, GetRunsUseCase {

    private final RunRepository runRepository;
    private final UserRepository userRepository;

    @Override
    public UUID save(SaveRunCommand command) {
        User user = getUser(command.userEmail());

        // 멱등: 이미 업로드된 런이면 새로 만들지 않고 기존 id 반환.
        var existing = runRepository.findIdByUserIdAndClientRunId(user.getId(), command.clientRunId());
        if (existing.isPresent()) {
            log.info("Run already uploaded (idempotent): userId={}, clientRunId={}",
                    user.getId(), command.clientRunId());
            return existing.get();
        }

        Run run = toRun(command, user.getId());
        try {
            UUID runId = runRepository.save(run);
            log.info("Run saved: runId={}, userId={}, distanceKm={}, points={}",
                    runId, user.getId(), command.distanceKm(), command.route().size());
            return runId;
        } catch (DataIntegrityViolationException e) {
            // 동시 업로드로 (user_id, client_run_id) 유니크 제약에 걸린 경우: 기존 id 반환.
            return runRepository.findIdByUserIdAndClientRunId(user.getId(), command.clientRunId())
                    .orElseThrow(() -> e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Run> getUserRuns(String userEmail) {
        return runRepository.findSummariesByUserId(getUser(userEmail).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Run getUserRun(String userEmail, UUID runId) {
        return runRepository.findByIdAndUserIdWithRoute(runId, getUser(userEmail).getId())
                .orElseThrow(() -> new NoSuchElementException("러닝 기록을 찾을 수 없습니다: " + runId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
    }

    private Run toRun(SaveRunCommand command, UUID userId) {
        AtomicInteger seq = new AtomicInteger(0);
        List<RunPoint> route = command.route().stream()
                .map(p -> RunPoint.builder()
                        .seq(seq.getAndIncrement())
                        .lat(p.lat())
                        .lng(p.lng())
                        .accuracy(p.accuracy())
                        .recordedAt(toUtc(Instant.ofEpochMilli(p.t())))
                        .build())
                .toList();

        return Run.builder()
                .userId(userId)
                .clientRunId(command.clientRunId())
                .groupId(command.groupId())
                .runnerId(command.runnerId())
                .color(command.color())
                .startedAt(toUtc(command.startedAt()))
                .endedAt(toUtc(command.endedAt()))
                .durationSec(command.durationSec())
                .distanceKm(command.distanceKm())
                .avgPaceSecPerKm(command.avgPaceSecPerKm())
                .route(route)
                .build();
    }

    private static LocalDateTime toUtc(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
