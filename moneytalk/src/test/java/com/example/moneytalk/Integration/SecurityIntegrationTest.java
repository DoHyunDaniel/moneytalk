package com.example.moneytalk.Integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;

import com.amazonaws.services.s3.AmazonS3;
import com.example.moneytalk.config.JwtTokenProvider;
import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private AmazonS3 amazonS3;
    
    @MockBean
    private S3Uploader s3Uploader;

    @Test
    void 인증없이_접근시_401반환() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void 인증된유저는_정상조회됨() throws Exception {
        // given
        String token = "valid.jwt.token";
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("tester")
                .password("encoded")
                .build();

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getUserId(token)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("tester"));
    }
}
