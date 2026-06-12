package com.runmarket.pacer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetEmailRequest(
        @NotBlank @Email String email,
        @NotBlank String turnstileToken
) {}
