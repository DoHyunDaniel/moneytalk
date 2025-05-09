package com.example.moneytalk.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestUri = request.getRequestURI();
		log.info("🔥 JwtAuthenticationFilter 작동 - URI: {}", request.getRequestURI());
		// WebSocket 연결은 토큰 검사 패스
		if (requestUri.startsWith("/ws-stomp") || requestUri.equals("/api/users/signup")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = resolveToken(request);

		if (token != null && jwtTokenProvider.validateToken(token)) {
			Long userId = jwtTokenProvider.getUserId(token);
			User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

			// user는 이미 UserDetails 구현체이므로 바로 사용 가능
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
					user.getAuthorities());

			// 인증 상세 정보 설정 (IP, 세션 등)
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.info("인증된 유저: {}", user.getEmail());
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		// 1. Authorization 헤더 검사
		String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}

		// 2. Cookie에서 "token" 항목 찾기
		if (request.getCookies() != null) {
			for (var cookie : request.getCookies()) {
				if ("token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

}
