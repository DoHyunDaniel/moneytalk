package com.example.moneytalk.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LoginRequest;
import com.example.moneytalk.dto.LoginResponse;
import com.example.moneytalk.dto.SignUpRequest;
import com.example.moneytalk.dto.SignUpResponse;
import com.example.moneytalk.dto.UserInfoResponse;
import com.example.moneytalk.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;	
	
	
	public SignUpResponse signUp(SignUpRequest request) {
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

	    return new SignUpResponse(saved.getId(), saved.getEmail(), saved.getNickname());
	}

	
	public LoginResponse signIn(LoginRequest request) {
	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
	    }

	    String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

	    return new LoginResponse(token, user.getEmail(), user.getNickname());
	}
	
	public UserInfoResponse getMyInfo(User user) {
	    return new UserInfoResponse(user.getId(), user.getEmail(), user.getNickname());
	}

	public void updateNickname(User user, String nickname) {
	    user.setNickname(nickname);
	    userRepository.save(user);
	}

	public void deleteUser(User user) {
	    userRepository.delete(user);
	}


}
