package com.example.moneytalk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.moneytalk.config.JwtCookieProvider;
import com.example.moneytalk.dto.LoginRequestDto;
import com.example.moneytalk.dto.LoginResponseDto;
import com.example.moneytalk.dto.NicknameSuggestionResponseDto;
import com.example.moneytalk.dto.SignUpRequestDto;
import com.example.moneytalk.dto.SignUpResponseDto;
import com.example.moneytalk.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
@Import({JwtCookieProvider.class, UserControllerTest.TestConfig.class})
class UserControllerTest {
	@TestConfiguration
	static class TestConfig {
	    @Bean
	    public UserDetailsService userDetailsService() {
	        UserDetails user = User.builder()
	            .username("user@example.com")
	            .password("{noop}password") // {noop}은 패스워드 인코딩을 하지 않겠다는 뜻!
	            .roles("USER")
	            .build();
	        return new InMemoryUserDetailsManager(user);
	    }
	}
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtCookieProvider jwtCookieProvider;

	@Nested
	@DisplayName("회원가입 테스트")
	class SignUpTest {

		@Test
		@WithMockUser
		@DisplayName("회원가입 성공")
		void signUpSuccess() throws Exception {
			// 준비
			SignUpRequestDto request = SignUpRequestDto.builder().email("user@example.com")
					.password("securePassword123!").nickname("dohyunnn").build();

			SignUpResponseDto response = SignUpResponseDto.builder().userId(1L).email("user@example.com")
					.nickname("dohyunnn").build();

			given(userService.signUp(any(SignUpRequestDto.class))).willReturn(response);

			// 실행 & 검증
			mockMvc.perform(post("/api/users/signup").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isOk())
					.andExpect(jsonPath("$.userId").value(1L)).andExpect(jsonPath("$.email").value("user@example.com"))
					.andExpect(jsonPath("$.nickname").value("dohyunnn"));
		}
	}

	@Nested
	@DisplayName("닉네임 추천 테스트")
	class SuggestNicknameTest {

		@Test
		@WithMockUser
		@DisplayName("닉네임 사용 가능 - 추천 없이 바로 사용 가능")
		void suggestNicknameAvailable() throws Exception {
			// given
			NicknameSuggestionResponseDto response = NicknameSuggestionResponseDto.builder().base("dohyunnn")
					.available(true).suggestions(Collections.emptyList()).build();
			given(userService.suggestNickname("dohyunnn")).willReturn(response);

			// when & then
			mockMvc.perform(get("/api/users/suggest-nickname").param("base", "dohyunnn")).andExpect(status().isOk())
					.andExpect(jsonPath("$.base").value("dohyunnn")).andExpect(jsonPath("$.available").value(true))
					.andExpect(jsonPath("$.suggestions").isEmpty());
		}

		@Test
		@WithMockUser
		@DisplayName("닉네임 중복 - 추천 닉네임 리스트 제공")
		void suggestNicknameWithSuggestions() throws Exception {
			// given
			NicknameSuggestionResponseDto response = NicknameSuggestionResponseDto.builder().base("dohyunnn")
					.available(false).suggestions(List.of("dohyunnn1", "dohyunnn99", "dohyunnn_dev")).build();

			given(userService.suggestNickname("dohyunnn")).willReturn(response);

			// when & then
			mockMvc.perform(get("/api/users/suggest-nickname").param("base", "dohyunnn")).andExpect(status().isOk())
					.andExpect(jsonPath("$.base").value("dohyunnn")).andExpect(jsonPath("$.available").value(false))
					.andExpect(jsonPath("$.suggestions").isArray())
					.andExpect(jsonPath("$.suggestions[0]").value("dohyunnn1"))
					.andExpect(jsonPath("$.suggestions[1]").value("dohyunnn99"))
					.andExpect(jsonPath("$.suggestions[2]").value("dohyunnn_dev"));
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class LoginTest {

		@Test
		@WithMockUser
		@DisplayName("정상 로그인 성공 시 토큰 쿠키와 응답 본문을 반환한다")
		void loginSuccess() throws Exception {
			// given
			LoginRequestDto request = LoginRequestDto.builder().email("user@example.com").password("securePassword123!")
					.build();

			LoginResponseDto response = LoginResponseDto.builder().token("fake-jwt-token").email("user@example.com")
					.nickname("dohyunnn").build();

			given(userService.signIn(any(LoginRequestDto.class))).willReturn(response);
			given(jwtCookieProvider.createTokenCookie(anyString()))
					.willReturn(ResponseCookie.from("token", "fake-jwt-token").build());

			// when & then
			mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isOk())
					.andExpect(header().exists(HttpHeaders.SET_COOKIE))
					.andExpect(jsonPath("$.token").value("fake-jwt-token"))
					.andExpect(jsonPath("$.email").value("user@example.com"))
					.andExpect(jsonPath("$.nickname").value("dohyunnn"));
		}
	}

	@Nested
	@DisplayName("로그아웃 테스트")
	class LogoutTest {

		@Test
		@WithMockUser
		@DisplayName("성공: JWT 쿠키 삭제 후 204 응답")
		void logout_success() throws Exception {
			// given
			ResponseCookie deleteCookie = ResponseCookie.from("token", "").path("/").httpOnly(true).secure(true)
					.maxAge(0).build();

			given(jwtCookieProvider.deleteTokenCookie()).willReturn(deleteCookie);

			// when & then
			mockMvc.perform(post("/api/users/logout").with(csrf())).andExpect(status().isNoContent())
					.andExpect(header().exists(HttpHeaders.SET_COOKIE)); // 쿠키 삭제 헤더 존재 확인

			verify(jwtCookieProvider, times(1)).deleteTokenCookie();
		}
	}

	@Nested
	@DisplayName("내 정보 조회 테스트")
	class GetMyInfoTest {
		// ✨ 작성 예정
	}

	@Nested
	@DisplayName("닉네임 수정 테스트")
	class UpdateNicknameTest {
		// ✨ 작성 예정
	}

	@Nested
	@DisplayName("회원 탈퇴 테스트")
	class DeleteUserTest {
		// ✨ 작성 예정
	}
}
