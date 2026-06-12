package com.runmarket.pacer.domain.port.out.email;

public interface EmailPort {
    void sendVerificationEmail(String to, String verificationLink);
    void sendPasswordResetEmail(String to, String resetLink);
}
