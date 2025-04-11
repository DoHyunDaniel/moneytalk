package com.example.moneytalk.config;

import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
    	
        // OAuth2User에서 이메일 꺼내기
        String email = ((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal()).getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        // Redirect URL 구성
        String redirectUrl = "http://localhost:3000/login-success?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
