package com.example.moneytalk.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable())
		        .logout(logout -> logout.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/users/signup", "/api/users/login",
						"/api/users/suggest-nickname", "/oauth2/**", "/v3/api-docs/**", "/swagger-ui/**",
						"/swagger-ui.html", "/docs.html", "/moneytalk-logo.png", "/favicon.png", "/ws-stomp/**",
						"/api/chat/**", "/pub/**", "/sub/**").permitAll().anyRequest().authenticated())
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"message\": \"로그인이 필요합니다.\"}");
				})).oauth2Login(oauth -> oauth
						// /login-success?token=...방식으로 프론트에서 처리
						.loginPage("/oauth2/authorization/google") // Spring Security 기본 loginPage 대신
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(oAuth2LoginSuccessHandler))
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
						UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("*"); // 모든 접근 허용
		config.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트 주소
		config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true); // httpOnly 쿠키 위해 필수

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

}
