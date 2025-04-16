package com.example.moneytalk.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LoginRequestDto;
import com.example.moneytalk.dto.LoginResponseDto;
import com.example.moneytalk.dto.SignUpRequestDto;
import com.example.moneytalk.dto.SignUpResponseDto;
import com.example.moneytalk.dto.UserInfoResponseDto;
import com.example.moneytalk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
/**
 * UserService
 * 사용자 인증 및 정보 관리를 담당하는 서비스입니다.
 *
 * [기능 설명]
 * - 회원 가입 및 이메일 중복 확인
 * - 로그인 처리 및 JWT 발급
 * - 사용자 정보 조회, 닉네임 수정, 회원 탈퇴
 *
 * [기술 요소]
 * - 비밀번호 암호화: {@link PasswordEncoder}
 * - JWT 발급: {@link JwtTokenProvider}
 *
 * [예외 처리]
 * - 이메일 중복, 존재하지 않는 사용자, 비밀번호 불일치 등 예외 처리 포함
 *
 * @author Daniel
 * @since 2025.04.15
 */
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;	
	
    /**
     * 사용자를 회원가입시킵니다.
     *
     * @param request 회원가입 요청 DTO (이메일, 비밀번호, 닉네임 포함)
     * @return 회원가입 결과 응답 DTO
     * @throws IllegalArgumentException 이미 등록된 이메일인 경우
     */
	public SignUpResponseDto signUp(SignUpRequestDto request) {
	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
	    }

	    User user = User.builder()
	            .email(request.getEmail())
	            .password(passwordEncoder.encode(request.getPassword()))
	            .nickname(request.getNickname())
	            .role("USER")
	            .build();

	    User saved = userRepository.save(user);

	    return new SignUpResponseDto(saved.getId(), saved.getEmail(), saved.getNickname());
	}

	
    /**
     * 로그인 요청을 처리하고 JWT 토큰을 발급합니다.
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return JWT 토큰, 이메일, 닉네임을 포함한 응답 DTO
     * @throws IllegalArgumentException 이메일 존재 여부 또는 비밀번호 불일치 시
     */
	public LoginResponseDto signIn(LoginRequestDto request) {
	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
	    }

	    String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

	    return new LoginResponseDto(token, user.getEmail(), user.getNickname());
	}
	
	
    /**
     * 현재 로그인한 사용자 정보를 조회합니다.
     *
     * @param user 인증된 사용자 객체
     * @return 사용자 정보 응답 DTO (id, email, nickname)
     */
	public UserInfoResponseDto getMyInfo(User user) {
	    return new UserInfoResponseDto(user.getId(), user.getEmail(), user.getNickname());
	}

	
    /**
     * 사용자 닉네임을 수정합니다.
     *
     * @param user 대상 사용자
     * @param nickname 새 닉네임
     */
	public void updateNickname(User user, String nickname) {
	    user.setNickname(nickname);
	    userRepository.save(user);
	}

	
    /**
     * 사용자 계정을 삭제(탈퇴)합니다.
     *
     * @param user 삭제할 사용자
     */
	public void deleteUser(User user) {
	    userRepository.delete(user);
	}


}
