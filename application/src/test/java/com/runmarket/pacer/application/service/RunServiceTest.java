package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.model.Run;
import com.runmarket.pacer.domain.model.User;
import com.runmarket.pacer.domain.port.in.run.SaveRunCommand;
import com.runmarket.pacer.domain.port.out.run.RunRepository;
import com.runmarket.pacer.domain.port.out.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunServiceTest {

    @Mock
    private RunRepository runRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RunService service;

    private static final String EMAIL = "runner@runmarket.cc";
    private static final Instant START = Instant.parse("2026-06-15T00:00:00Z");

    private SaveRunCommand command(String clientRunId) {
        return new SaveRunCommand(
                EMAIL,
                clientRunId,
                "GROUP1",
                "alice",
                START,
                START.plusSeconds(120),
                120,
                0.42,
                286,
                "#ff9900",
                List.of(
                        new SaveRunCommand.RoutePoint(37.5665, 126.9780, START.toEpochMilli(), 5.0),
                        new SaveRunCommand.RoutePoint(37.5670, 126.9785, START.plusSeconds(3).toEpochMilli(), null),
                        new SaveRunCommand.RoutePoint(37.5675, 126.9790, START.plusSeconds(6).toEpochMilli(), 8.5)
                )
        );
    }

    private User user(UUID id) {
        return User.builder().id(id).email(EMAIL).build();
    }

    @Test
    void save_newRun_persistsAndReturnsGeneratedId() {
        UUID userId = UUID.randomUUID();
        UUID generatedId = UUID.randomUUID();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(userId)));
        when(runRepository.findIdByUserIdAndClientRunId(userId, "alice-1000")).thenReturn(Optional.empty());
        when(runRepository.save(any(Run.class))).thenReturn(generatedId);

        UUID result = service.save(command("alice-1000"));

        assertThat(result).isEqualTo(generatedId);
    }

    @Test
    void save_newRun_mapsFieldsRouteSeqAndUtcTimestamps() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(userId)));
        when(runRepository.findIdByUserIdAndClientRunId(userId, "alice-1000")).thenReturn(Optional.empty());
        when(runRepository.save(any(Run.class))).thenReturn(UUID.randomUUID());

        service.save(command("alice-1000"));

        ArgumentCaptor<Run> captor = ArgumentCaptor.forClass(Run.class);
        verify(runRepository).save(captor.capture());
        Run saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getClientRunId()).isEqualTo("alice-1000");
        assertThat(saved.getGroupId()).isEqualTo("GROUP1");
        assertThat(saved.getRunnerId()).isEqualTo("alice");
        assertThat(saved.getColor()).isEqualTo("#ff9900");
        assertThat(saved.getDurationSec()).isEqualTo(120);
        assertThat(saved.getDistanceKm()).isEqualTo(0.42);
        assertThat(saved.getAvgPaceSecPerKm()).isEqualTo(286);
        // Instant → UTC LocalDateTime
        assertThat(saved.getStartedAt()).isEqualTo(LocalDateTime.ofInstant(START, ZoneOffset.UTC));
        assertThat(saved.getEndedAt()).isEqualTo(LocalDateTime.ofInstant(START.plusSeconds(120), ZoneOffset.UTC));

        assertThat(saved.getRoute()).hasSize(3);
        // seq는 0부터 순서대로 부여된다
        assertThat(saved.getRoute()).extracting("seq").containsExactly(0, 1, 2);
        // 정확도는 그대로(누락 시 null) 전달된다
        assertThat(saved.getRoute()).extracting("accuracy").containsExactly(5.0, null, 8.5);
        // epoch ms → UTC LocalDateTime
        assertThat(saved.getRoute().get(1).getRecordedAt())
                .isEqualTo(LocalDateTime.ofInstant(START.plusSeconds(3), ZoneOffset.UTC));
    }

    @Test
    void save_alreadyUploaded_returnsExistingIdWithoutSaving() {
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(userId)));
        when(runRepository.findIdByUserIdAndClientRunId(userId, "alice-1000"))
                .thenReturn(Optional.of(existingId));

        UUID result = service.save(command("alice-1000"));

        assertThat(result).isEqualTo(existingId);
        verify(runRepository, never()).save(any());
    }

    @Test
    void save_unknownUser_throwsNoSuchElement() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(command("alice-1000")))
                .isInstanceOf(NoSuchElementException.class);

        verify(runRepository, never()).save(any());
    }

    @Test
    void save_concurrentDuplicate_recoversExistingIdOnConstraintViolation() {
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(userId)));
        // 첫 조회는 없음 → 저장 시도 → 동시 업로드로 유니크 제약 위반 → 재조회 시 기존 id 존재
        when(runRepository.findIdByUserIdAndClientRunId(userId, "alice-1000"))
                .thenReturn(Optional.empty(), Optional.of(existingId));
        when(runRepository.save(any(Run.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        UUID result = service.save(command("alice-1000"));

        assertThat(result).isEqualTo(existingId);
        verify(runRepository).save(any(Run.class));
    }

    @Test
    void save_constraintViolationButNoExistingRow_rethrows() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(userId)));
        when(runRepository.findIdByUserIdAndClientRunId(eq(userId), eq("alice-1000")))
                .thenReturn(Optional.empty(), Optional.empty());
        when(runRepository.save(any(Run.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.save(command("alice-1000")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
