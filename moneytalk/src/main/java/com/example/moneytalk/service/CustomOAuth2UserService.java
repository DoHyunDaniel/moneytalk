package com.example.moneytalk.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * CustomOAuth2UserService
 * OAuth2 로그인 시 사용자 정보를 처리하는 서비스입니다.
 *
 * [기능 설명]
 * - OAuth2 제공자(Google 등)로부터 받은 사용자 정보를 기반으로
 *   DB에 사용자가 존재하면 불러오고, 없으면 자동으로 등록합니다.
 * - Spring Security에서 OAuth2User를 반환하여 인증 과정을 이어갑니다.
 *
 * @author 
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 정보를 불러오고, 필요한 경우 자동으로 사용자 등록을 수행합니다.
     *
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 인증된 사용자 정보를 담은 OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 예외 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User =
            new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 예: "google"
        String userNameAttributeName = "email";

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 사용자 존재 여부 확인 및 등록
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .nickname(name)
                .password("") // 소셜 로그인은 비밀번호 X
                .role("USER")
                .build()));

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            userNameAttributeName
        );
    }
}

