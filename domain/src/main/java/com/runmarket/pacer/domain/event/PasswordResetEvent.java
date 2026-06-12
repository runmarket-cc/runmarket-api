package com.runmarket.pacer.domain.event;

public record PasswordResetEvent(String email, String resetLink) {}
