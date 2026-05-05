package com.runmarket.pacer.infrastructure.turnstile;

import com.runmarket.pacer.domain.port.out.verification.CaptchaVerificationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class TurnstileAdapterLocal implements CaptchaVerificationPort {

    @Override
    public void verify(String token) {
        // Pass turnstile verify when if local profile
    }
}
