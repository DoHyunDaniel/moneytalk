package com.example.moneytalk.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LoginRequest;
import com.example.moneytalk.dto.LoginResponse;
import com.example.moneytalk.dto.SignUpRequest;
import com.example.moneytalk.dto.SignUpResponse;
import com.example.moneytalk.dto.UpdateNicknameRequest;
import com.example.moneytalk.dto.UserInfoResponse;
import com.example.moneytalk.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
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
	public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
	    LoginResponse loginResponse = userService.signIn(request);

	    // JWT를 httpOnly 쿠키로 세팅
	    ResponseCookie cookie = ResponseCookie.from("token", loginResponse.getToken())
	            .httpOnly(true)
	            .secure(false) // 배포 시 true (https)
	            .path("/")
	            .maxAge(60 * 60 * 24) // 1일
	            .sameSite("Lax")
	            .build();

	    return ResponseEntity.ok()
	            .header(HttpHeaders.SET_COOKIE, cookie.toString())
	            .body(Map.of(
	            		"token", loginResponse.getToken(),
	                    "email", loginResponse.getEmail(),
	                    "nickname", loginResponse.getNickname()
	            ));
	}

	@Operation(
		    summary = "로그아웃",
		    description = "JWT 쿠키를 삭제하여 로그아웃 처리합니다.",
		    security = @SecurityRequirement(name = "bearerAuth")
		)
		@SecurityRequirement(name = "bearerAuth")
		@PostMapping("/logout")
		public ResponseEntity<Void> logout(HttpServletResponse response) {
		    ResponseCookie deleteCookie = ResponseCookie.from("token", "")
		            .path("/")
		            .maxAge(0)
		            .httpOnly(true)
		            .build();

		    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

		    return ResponseEntity.noContent().build();
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
