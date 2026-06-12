package com.runmarket.pacer.domain.port.in.auth;

public record ResetPasswordCommand(String token, String newPassword) {}
