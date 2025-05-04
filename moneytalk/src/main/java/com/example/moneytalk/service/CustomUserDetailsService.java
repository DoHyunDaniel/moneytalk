package com.example.moneytalk.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * CustomUserDetailsService
 * Spring Security에서 사용자 인증 정보를 처리하는 서비스입니다.
 *
 * [기능 설명]
 * - 일반 로그인(이메일 기반) 시, 사용자의 상세 정보를 불러와 인증을 수행합니다.
 * - 추가적으로, JWT 인증 시 userId 기반으로 사용자 정보를 조회할 수 있습니다.
 *
 * implements {@link org.springframework.security.core.userdetails.UserDetailsService}
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일을 기반으로 사용자를 조회하여 인증 정보를 반환합니다.
     *
     * @param email 사용자 이메일
     * @return UserDetails 객체 (Spring Security 인증 정보)
     * @throws GlobalException 사용자가 존재하지 않을 경우 발생 {@link ErrorCode#USER_NOT_FOUND}
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * userId를 기반으로 사용자를 조회하여 인증 정보를 반환합니다.
     * (주로 JWT 인증 토큰 처리 시 사용)
     *
     * @param userId 사용자 ID
     * @return UserDetails 객체
     * @throws GlobalException 사용자가 존재하지 않을 경우 발생 {@link ErrorCode#USER_NOT_FOUND}
     */
    public UserDetails loadUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }
}
