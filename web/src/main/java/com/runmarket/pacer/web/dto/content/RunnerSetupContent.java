package com.runmarket.pacer.web.dto.content;

/** "러너로 달리기" 설정 화면(runner.tsx)의 정적 문구 */
public record RunnerSetupContent(
        InfoCardContent info,
        InputFieldContent groupCode,
        InputFieldContent runnerId,
        String colorLabel,
        String colorAutoText,
        String colorHint,
        String colorModalTitle,
        String colorModalDesc,
        String confirmButton,
        String cancelButton,
        String startButton,
        AlertContent emptyFieldsAlert,
        AlertContent tokenFailAlert
) {}
