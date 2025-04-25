package com.example.moneytalk.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.config.JwtCookieProvider;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LoginRequestDto;
import com.example.moneytalk.dto.LoginResponseDto;
import com.example.moneytalk.dto.NicknameSuggestionResponseDto;
import com.example.moneytalk.dto.SignUpRequestDto;
import com.example.moneytalk.dto.SignUpResponseDto;
import com.example.moneytalk.dto.UpdateNicknameRequestDto;
import com.example.moneytalk.dto.UserInfoResponseDto;
import com.example.moneytalk.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * UserController 사용자 인증 및 계정 관리와 관련된 API를 제공하는 컨트롤러입니다.
 *
 * [기능 설명] - 회원가입, 로그인(JWT 발급), 로그아웃 - 사용자 정보 조회 및 닉네임 수정 - 회원 탈퇴
 *
 * [보안] - `/signup`, `/login`은 인증 불필요 - `/logout`, `/me`, `/me(수정/삭제)`는 JWT 인증
 * 필요 (`@AuthenticationPrincipal`을 통해 사용자 정보 추출)
 *
 * [인증 방식] - JWT는 httpOnly 쿠키에 저장되며, Swagger 테스트 시 "Authorize"로 헤더 인증 가능
 *
 * @author Daniel
 * @since 2025.04.15
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final JwtCookieProvider jwtCookieProvider;

	// ──────────────── 인증 & 회원가입 ────────────────

	@Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 통해 회원가입을 수행합니다.")
	@PostMapping("/signup")
	public ResponseEntity<SignUpResponseDto> signUp(@RequestBody @Valid SignUpRequestDto request) {
		SignUpResponseDto response = userService.signUp(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "새로운 닉네임 추천", description = "회원가입 시 닉네임이 중복될 경우 새로운 닉네임을 추천합니다.")
	@GetMapping("/suggest-nickname")
	public ResponseEntity<NicknameSuggestionResponseDto> suggestNickname(
	        @RequestParam("base") String base) {
	    return ResponseEntity.ok(userService.suggestNickname(base));
	}
	
	@Operation(summary = "로그인", description = "이메일과 비밀번호를 통해 JWT 토큰을 발급받습니다.")
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request, HttpServletResponse response) {
		LoginResponseDto loginResponse = userService.signIn(request);

		ResponseCookie cookie = jwtCookieProvider.createTokenCookie(loginResponse.getToken());

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginResponse);
	}


	@Operation(summary = "로그아웃", description = "JWT 쿠키를 삭제하여 로그아웃 처리합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		ResponseCookie deleteCookie = jwtCookieProvider.deleteTokenCookie();
		response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
		return ResponseEntity.noContent().build();
	}

	// ──────────────── 사용자 정보 관리 ────────────────

	@Operation(summary = "내 정보 조회", description = "JWT 인증 토큰을 통해 로그인한 사용자의 정보를 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/me")
	public ResponseEntity<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(userService.getMyInfo(user));
	}

	@Operation(summary = "닉네임 변경", description = "로그인한 사용자의 닉네임을 수정합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@SecurityRequirement(name = "bearerAuth")
	@PatchMapping("/me")
	public ResponseEntity<Void> updateNickname(@AuthenticationPrincipal User user,
			@RequestBody @Valid UpdateNicknameRequestDto request) {
		userService.updateNickname(user, request.getNickname());
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "회원 탈퇴", description = "JWT 인증 토큰을 통해 로그인한 사용자의 계정을 삭제합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User user) {
		userService.deleteUser(user);
		return ResponseEntity.noContent().build();
	}
}
