package com.runmarket.pacer.web.dto.content;

/** "관전하기" 설정 화면(spectator.tsx)의 정적 문구 */
public record SpectatorSetupContent(
        InfoCardContent info,
        InputFieldContent groupCode,
        String watchButton,
        AlertContent emptyFieldsAlert,
        AlertContent tokenFailAlert
) {}
