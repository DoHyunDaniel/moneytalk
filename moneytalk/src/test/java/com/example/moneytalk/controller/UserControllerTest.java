package com.example.moneytalk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.moneytalk.config.JwtAuthenticationFilter;
import com.example.moneytalk.config.JwtCookieProvider;
import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.dto.LoginRequestDto;
import com.example.moneytalk.dto.LoginResponseDto;
import com.example.moneytalk.dto.NicknameSuggestionResponseDto;
import com.example.moneytalk.dto.SignUpRequestDto;
import com.example.moneytalk.dto.SignUpResponseDto;
import com.example.moneytalk.dto.UpdateNicknameRequestDto;
import com.example.moneytalk.dto.UserInfoResponseDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.service.UserService;
import com.example.moneytalk.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

	@AfterEach
	void tearDown() {
	    reset(userService, jwtTokenProvider, userRepository);
	}
	
	@TestConfiguration
	static class TestConfig {
		@Bean
		public UserDetailsService userDetailsService() {
			UserDetails user = User.builder().username("user@example.com").password("{noop}password").roles("USER")
					.build();
			return new InMemoryUserDetailsManager(user);
		}
	}

	@TestConfiguration
	static class TestSecurityConfig {
		private static boolean filterRegistered = false;

		@Autowired
		JwtTokenProvider jwtTokenProvider;

		@Autowired
		UserRepository userRepository;

		@Bean
		public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
			if (!filterRegistered) {
				http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
						UsernamePasswordAuthenticationFilter.class);
				filterRegistered = true;
			}
			return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
					.build();
		}
	}

	private com.example.moneytalk.domain.User mockUser() {
		return com.example.moneytalk.domain.User.builder().id(1L).email("user@example.com").nickname("dohyunnn")
				.password("encodedPassword").build();
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtCookieProvider jwtCookieProvider;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

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

		@Test
		@DisplayName("내 정보 조회 성공 (JWT 인증)")
		void getMyInfoSuccess() throws Exception {
			// given
			Long userId = 1L;
			String email = "user@example.com";
			String token = jwtTokenProvider.createToken(userId, email);
			com.example.moneytalk.domain.User mockUser = mockUser();

			// 토큰 유효성 검사 통과
			given(jwtTokenProvider.validateToken(anyString())).willReturn(true);

			// 토큰으로 userId 추출
			given(jwtTokenProvider.getUserId(anyString())).willReturn(1L);

			// userId로 유저 조회
			given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

			// 🔥 핵심: userService.getMyInfo(...)에 대한 응답 설정
			given(userService.getMyInfo(any()))
					.willReturn(UserInfoResponseDto.builder().userId(userId).email(email).nickname("dohyunnn").build());

			// when & then
			mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).with(csrf()))
					.andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1L))
					.andExpect(jsonPath("$.email").value("user@example.com"))
					.andExpect(jsonPath("$.nickname").value("dohyunnn")).andDo(print()); // 응답 JSON 확인용
		}

	}

	@Nested
	@DisplayName("닉네임 수정 테스트")
	class UpdateNicknameTest {

		@Test
		@DisplayName("닉네임 수정 성공")
		void updateNicknameSuccess() throws Exception {
			UpdateNicknameRequestDto request = UpdateNicknameRequestDto.builder().nickname("hyunnnn").build();

			doNothing().when(userService).updateNickname(any(), anyString());

			mockMvc.perform(patch("/api/users/me").with(csrf()).with(user(mockUser()))
					.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNoContent()).andDo(print());

			verify(userService, times(1)).updateNickname(any(), anyString());
		}
	}

	@Nested
	@DisplayName("회원 탈퇴 테스트")
	class DeleteUserTest {

		@Test
		@DisplayName("회원 탈퇴 성공 (JWT 인증)")
		void deleteUserSuccess() throws Exception {
			// given
			Long userId = 1L;
			String email = "user@example.com";
			String token = jwtTokenProvider.createToken(userId, email);

			com.example.moneytalk.domain.User mockUser = mockUser();

			given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
			given(jwtTokenProvider.getUserId(anyString())).willReturn(userId);
			given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

			doAnswer(invocation -> {
				return null;
			}).when(userService).deleteUser(any());

			// when & then
			mockMvc.perform(delete("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).with(csrf()))
					.andExpect(status().isNoContent());

			verify(userService, times(1)).deleteUser(any());
		}

	}

	@Nested
	@DisplayName("회원가입 실패 테스트")
	class SignUpFailTest {

		@Test
		@WithMockUser
		@DisplayName("이미 사용 중인 이메일로 회원가입 시 409 반환")
		void signUpWithDuplicateEmail() throws Exception {
			// given
			SignUpRequestDto request = SignUpRequestDto.builder().email("user@example.com").password("password123!")
					.nickname("newbie").build();

			given(userService.signUp(any(SignUpRequestDto.class)))
					.willThrow(new GlobalException(ErrorCode.EMAIL_ALREADY_EXISTS));

			// when & then
			mockMvc.perform(post("/api/users/signup").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isConflict()) // 409
					.andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
		}

		@Test
		@WithMockUser
		@DisplayName("이미 사용 중인 닉네임으로 회원가입 시 409 반환")
		void signUpWithDuplicateNickname() throws Exception {
			// given
			SignUpRequestDto request = SignUpRequestDto.builder().email("new@example.com").password("password123!")
					.nickname("dohyunnn").build();

			given(userService.signUp(any(SignUpRequestDto.class)))
					.willThrow(new GlobalException(ErrorCode.NICKNAME_ALREADY_EXISTS));

			// when & then
			mockMvc.perform(post("/api/users/signup").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isConflict()) // 409
					.andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
		}
	}

	@Nested
	@DisplayName("인증 실패 테스트")
	class UnauthorizedAccessTest {

		@Test
		@WithAnonymousUser
		@DisplayName("로그인하지 않고 /me 요청 시 401 Unauthorized")
		void getMyInfoWithoutLogin() throws Exception {
			mockMvc.perform(get("/api/users/me").with(csrf())).andExpect(status().isUnauthorized());
		}

		@Test
		@WithAnonymousUser
		@DisplayName("로그인하지 않고 닉네임 수정 시 401 Unauthorized")
		void updateNicknameWithoutLogin() throws Exception {
			UpdateNicknameRequestDto request = UpdateNicknameRequestDto.builder().nickname("newnick").build();

			mockMvc.perform(patch("/api/users/me").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf()))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@WithAnonymousUser
		@DisplayName("로그인하지 않고 회원 탈퇴 요청 시 401 Unauthorized")
		void deleteUserWithoutLogin() throws Exception {
			mockMvc.perform(delete("/api/users/me").with(csrf())).andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("입력값 검증 실패 테스트")
	class ValidationFailTest {

		@Test
		@WithMockUser
		@DisplayName("닉네임이 null일 경우 400 Bad Request")
		void updateNicknameNull() throws Exception {
			String invalidJson = "{\"nickname\": null}";

			mockMvc.perform(
					patch("/api/users/me").contentType(MediaType.APPLICATION_JSON).content(invalidJson).with(csrf()))
					.andExpect(status().isBadRequest());
		}

		@Test
		@WithMockUser
		@DisplayName("닉네임이 공백일 경우 400 Bad Request")
		void updateNicknameBlank() throws Exception {
			UpdateNicknameRequestDto request = UpdateNicknameRequestDto.builder().nickname(" ").build();

			mockMvc.perform(patch("/api/users/me").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isBadRequest());
		}

		@Test
		@WithMockUser
		@DisplayName("회원가입 시 잘못된 이메일 포맷 → 400")
		void signUpWithInvalidEmailFormat() throws Exception {
			SignUpRequestDto request = SignUpRequestDto.builder().email("invalid-email") // 이메일 형식 아님
					.password("password123!").nickname("nick").build();

			mockMvc.perform(post("/api/users/signup").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("예상치 못한 예외 발생 테스트")
	class UnexpectedErrorTest {

		@Test
		@WithMockUser
		@DisplayName("내 정보 조회 중 NullPointerException 발생 시 500 반환")
		void getMyInfoInternalError() throws Exception {
			given(userService.getMyInfo(any())).willThrow(new NullPointerException("테스트용 NPE"));

			mockMvc.perform(get("/api/users/me").with(csrf())).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다.")); // GlobalExceptionHandler
		}

		@Test
		@WithMockUser
		@DisplayName("회원 탈퇴 중 예상치 못한 RuntimeException 발생 시 500 반환")
		void deleteUserThrowsUnexpectedError() throws Exception {
			doThrow(new RuntimeException("DB 삭제 실패")).when(userService).deleteUser(any());

			mockMvc.perform(delete("/api/users/me").with(csrf())).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
		}
	}

	private RequestPostProcessor authenticatedUser(com.example.moneytalk.domain.User user) {
		return request -> {
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
					user.getAuthorities());
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(auth);
			SecurityContextHolder.setContext(context);
			return request;
		};
	}

}
