package com.runmarket.pacer.web.controller;

import com.runmarket.pacer.domain.port.in.run.SaveRunCommand;
import com.runmarket.pacer.domain.port.in.run.SaveRunUseCase;
import com.runmarket.pacer.web.dto.RunResponse;
import com.runmarket.pacer.web.dto.SaveRunRequest;
import com.runmarket.pacer.web.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
public class RunController {

    private final SaveRunUseCase saveRunUseCase;

    @PostMapping
    public ResponseEntity<RunResponse> saveRun(@Valid @RequestBody SaveRunRequest request) {
        UUID runId = saveRunUseCase.save(new SaveRunCommand(
                SecurityUtils.currentUserEmail(),
                request.clientRunId(),
                request.groupId(),
                request.runnerId(),
                request.startedAt(),
                request.endedAt(),
                request.durationSec(),
                request.distanceKm(),
                request.avgPaceSecPerKm(),
                request.color(),
                request.route().stream()
                        .map(p -> new SaveRunCommand.RoutePoint(p.lat(), p.lng(), p.t(), p.acc()))
                        .toList()
        ));
        return ResponseEntity.ok(new RunResponse(runId.toString()));
    }
}
