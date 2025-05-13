package com.example.moneytalk.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.UserRepository;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
    }
    
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
   
    @Test
    void doFilterInternal_정상토큰_인증성공() throws Exception {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;

        User mockUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("tester")
                .password("encoded")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getUserId(token)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(mockUser);
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    void doFilterInternal_토큰없음_인증안됨() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull(); // 인증 안 됨
    }

    @Test
    void doFilterInternal_토큰유효하지않음_인증안됨() throws Exception {
        // given
        String token = "invalid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull(); // 인증 실패
    }

    @Test
    void doFilterInternal_쿠키에토큰_인증성공() throws Exception {
        // given
        String token = "cookie.jwt.token";
        Long userId = 10L;

        User mockUser = User.builder().id(userId).email("abc@money.com").nickname("쿠키유저").build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        request.setCookies(new jakarta.servlet.http.Cookie("token", token));

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getUserId(token)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(mockUser);
    }
}
