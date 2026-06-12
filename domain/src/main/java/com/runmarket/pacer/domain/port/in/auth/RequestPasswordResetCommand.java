package com.runmarket.pacer.domain.port.in.auth;

public record RequestPasswordResetCommand(String email, String captchaToken) {}
