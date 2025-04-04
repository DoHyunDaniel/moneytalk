package com.example.moneytalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.*;
import com.example.moneytalk.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@Operation(
		summary = "회원가입",
		description = "이메일, 비밀번호, 닉네임을 통해 회원가입을 수행합니다."
	)
	@PostMapping("/signup")
	public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
	    SignUpResponse response = userService.signUp(request);
	    return ResponseEntity.ok(response);
	}
	
	@Operation(
		summary = "로그인",
		description = "이메일과 비밀번호를 통해 JWT 토큰을 발급받습니다."
	)
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
		LoginResponse response = userService.signIn(request);
		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "내 정보 조회",
		description = "JWT 인증 토큰을 통해 로그인한 사용자의 정보를 조회합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/me")
	public ResponseEntity<UserInfoResponse> getMyInfo(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(userService.getMyInfo(user));
	}

	@Operation(
		summary = "닉네임 변경",
		description = "로그인한 사용자의 닉네임을 수정합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@SecurityRequirement(name = "bearerAuth")
	@PatchMapping("/me")
	public ResponseEntity<Void> updateNickname(@AuthenticationPrincipal User user,
			@RequestBody @Valid UpdateNicknameRequest request) {
		userService.updateNickname(user, request.getNickname());
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "회원 탈퇴",
		description = "JWT 인증 토큰을 통해 로그인한 사용자의 계정을 삭제합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User user) {
		userService.deleteUser(user);
		return ResponseEntity.noContent().build();
	}
}
