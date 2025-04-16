package com.example.moneytalk.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieProvider {

    private static final String COOKIE_NAME = "token";
    private static final Duration EXPIRATION = Duration.ofDays(1);

    @Value("${COOKIE_SECURE:false}") // 기본값 false
    private boolean isSecure;

    public ResponseCookie createTokenCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(EXPIRATION)
            .sameSite("Lax")
            .build();
    }

    public ResponseCookie deleteTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();
    }
}
