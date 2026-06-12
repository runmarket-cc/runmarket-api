package com.runmarket.pacer.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 요청(토큰 미존재 또는 만료된 로그인 세션)에 대해
 * 고객이 이해할 수 있는 메시지를 담은 JSON(ProblemDetail 형식) 401 응답을 반환한다.
 * 러너로 달리기/관전하기 진입 시(POST /api/v1/socket-token 등) 세션이 만료되면 여기로 진입한다.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String SESSION_EXPIRED_MESSAGE =
            "로그인 세션이 만료되었습니다. 다시 로그인한 후 이용해주세요.";

    private static final String BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"%s"}"""
            .formatted(SESSION_EXPIRED_MESSAGE);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(BODY);
    }
}
