package com.runmarket.pacer.web.controller;

import com.runmarket.pacer.web.dto.content.AlertContent;
import com.runmarket.pacer.web.dto.content.InfoCardContent;
import com.runmarket.pacer.web.dto.content.InputFieldContent;
import com.runmarket.pacer.web.dto.content.RunnerSetupContent;
import com.runmarket.pacer.web.dto.content.SpectatorSetupContent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 네이티브 화면(러너로 달리기 / 관전하기)에 표시되는 정적 안내 문구를 제공한다.
 * 인증 없이 접근 가능(permitAll). 문구는 코드 내 정적 상수로 관리한다.
 */
@RestController
@RequestMapping("/api/v1/contents")
public class ScreenContentController {

    private static final RunnerSetupContent RUNNER_SETUP = new RunnerSetupContent(
            new InfoCardContent(
                    "🏃",
                    "러너 모드",
                    "달리는 동안 내 위치가 실시간으로 관전자에게 공유됩니다."
            ),
            new InputFieldContent(
                    "그룹 코드",
                    "예: AAAA",
                    "관전자가 이 코드로 입장합니다. 함께 달릴 그룹의 고유 코드를 정하세요."
            ),
            new InputFieldContent(
                    "러너 ID",
                    "예: runner-1",
                    "같은 그룹 안에서 나를 구별하는 이름입니다."
            ),
            "내 색상",
            "자동 배정 (탭하여 변경)",
            "지도와 러너 목록에서 나를 표시할 색상입니다. 선택하지 않으면 러너 ID 기반으로 자동 배정됩니다.",
            "내 마커 색상 선택",
            "지도와 러너 목록에서 나를 나타낼 색상을 골라주세요.",
            "확인",
            "취소",
            "달리기 시작",
            new AlertContent("입력 오류", "그룹 코드와 러너 ID를 모두 입력해주세요."),
            new AlertContent("오류", "소켓 토큰 발급에 실패했습니다.")
    );

    private static final SpectatorSetupContent SPECTATOR_SETUP = new SpectatorSetupContent(
            new InfoCardContent(
                    "👀",
                    "관전 모드",
                    "그룹 코드를 입력하면 달리고 있는 러너들의 위치를 실시간 지도에서 확인할 수 있습니다."
            ),
            new InputFieldContent(
                    "그룹 코드",
                    "예: AAAA",
                    "러너에게 그룹 코드를 받아 입력하세요."
            ),
            "관전 시작",
            new AlertContent("입력 오류", "그룹 코드를 입력해주세요."),
            new AlertContent("오류", "소켓 토큰 발급에 실패했습니다.")
    );

    @GetMapping("/runner-setup")
    public ResponseEntity<RunnerSetupContent> runnerSetup() {
        return ResponseEntity.ok(RUNNER_SETUP);
    }

    @GetMapping("/spectator-setup")
    public ResponseEntity<SpectatorSetupContent> spectatorSetup() {
        return ResponseEntity.ok(SPECTATOR_SETUP);
    }
}
