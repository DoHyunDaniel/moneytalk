package com.example.moneytalk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.LoginRequestDto;
import com.example.moneytalk.dto.SignUpRequestDto;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.type.UserType;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenSignUpWithValidInput_thenReturnSignUpResponseDto() {
        // when
        SignUpRequestDto request = new SignUpRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("testuser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

        var response = userService.signUp(request);

        // then
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("testuser", response.getNickname());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void whenSignUpWithExistingEmail_thenThrowException() {
        // when
        SignUpRequestDto request = new SignUpRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("testuser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // then
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(request));
    }

    @Test
    void whenSuggestNicknameAvailable_thenReturnAvailableTrue() {
        // when
        String baseNickname = "uniqueNickname";
        when(userRepository.existsByNickname(baseNickname)).thenReturn(false);

        var response = userService.suggestNickname(baseNickname);

        // then
        assertTrue(response.isAvailable());
        assertEquals(baseNickname, response.getBase());
        assertTrue(response.getSuggestions().isEmpty());
    }

    @Test
    void whenSuggestNicknameNotAvailable_thenReturnSuggestions() {
        // when
        String baseNickname = "testuser";
        when(userRepository.existsByNickname(baseNickname)).thenReturn(true);
        when(userRepository.existsByNickname("testuser1")).thenReturn(false);

        var response = userService.suggestNickname(baseNickname);

        // then
        assertFalse(response.isAvailable());
        assertFalse(response.getSuggestions().isEmpty());
    }

    @Test
    void whenSignInWithValidCredentials_thenReturnLoginResponseDto() {
        // when
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testuser")
                .role(UserType.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(user.getId(), user.getEmail())).thenReturn("mocked-jwt-token");

        var response = userService.signIn(request);

        // then
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("testuser", response.getNickname());
    }

    @Test
    void whenSignInWithInvalidEmail_thenThrowException() {
        // when
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("wrong@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> userService.signIn(request));
    }

    @Test
    void whenSignInWithInvalidPassword_thenThrowException() {
        // when
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testuser")
                .role(UserType.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // then
        assertThrows(IllegalArgumentException.class, () -> userService.signIn(request));
    }
}
