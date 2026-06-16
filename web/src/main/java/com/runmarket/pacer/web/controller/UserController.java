package com.runmarket.pacer.web.controller;

import com.runmarket.pacer.domain.model.Race;
import com.runmarket.pacer.domain.port.in.race.GetLikedRacesUseCase;
import com.runmarket.pacer.domain.port.in.race.GetRaceLikeCountUseCase;
import com.runmarket.pacer.domain.port.in.run.GetRunsUseCase;
import com.runmarket.pacer.domain.port.in.user.WithdrawUseCase;
import com.runmarket.pacer.web.dto.RaceListItemResponse;
import com.runmarket.pacer.web.dto.RunDetailResponse;
import com.runmarket.pacer.web.dto.RunSummaryResponse;
import com.runmarket.pacer.web.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final WithdrawUseCase withdrawUseCase;
    private final GetLikedRacesUseCase getLikedRacesUseCase;
    private final GetRaceLikeCountUseCase getRaceLikeCountUseCase;
    private final GetRunsUseCase getRunsUseCase;

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw() {
        withdrawUseCase.withdraw(SecurityUtils.currentUserEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/liked-races")
    public ResponseEntity<List<RaceListItemResponse>> getLikedRaces() {
        List<Race> races = getLikedRacesUseCase.getLikedRaces(SecurityUtils.currentUserEmail());
        List<UUID> raceIds = races.stream().map(Race::getId).toList();
        Map<UUID, Long> likeCounts = getRaceLikeCountUseCase.getLikeCounts(raceIds);
        return ResponseEntity.ok(races.stream()
                .map(race -> RaceListItemResponse.from(race, likeCounts.getOrDefault(race.getId(), 0L), true))
                .toList());
    }

    @GetMapping("/me/runs")
    public ResponseEntity<List<RunSummaryResponse>> getMyRuns() {
        return ResponseEntity.ok(getRunsUseCase.getUserRuns(SecurityUtils.currentUserEmail()).stream()
                .map(RunSummaryResponse::from)
                .toList());
    }

    @GetMapping("/me/runs/{id}")
    public ResponseEntity<RunDetailResponse> getMyRun(@PathVariable UUID id) {
        return ResponseEntity.ok(
                RunDetailResponse.from(getRunsUseCase.getUserRun(SecurityUtils.currentUserEmail(), id)));
    }
}
