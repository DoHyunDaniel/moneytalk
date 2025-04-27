package com.example.moneytalk.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.type.UserType;

@SpringBootTest(properties = {
	    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
	    "spring.datasource.driver-class-name=org.h2.Driver",
	    "spring.datasource.username=sa",
	    "spring.datasource.password=",
	    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	    "spring.jpa.hibernate.ddl-auto=create",
	    "spring.jpa.show-sql=true",
	    "openai.api.key=dummy"
	})
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_success() {
        // given
        User user = createUser("test@example.com", "testnickname");
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getNickname()).isEqualTo("testnickname");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재할 때")
    void existsByEmail_true() {
        // given
        User user = createUser("exist@example.com", "nickname1");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("exist@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않을 때")
    void existsByEmail_false() {
        // when
        boolean exists = userRepository.existsByEmail("nonexist@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("닉네임 존재 여부 확인 - 존재할 때")
    void existsByNickname_true() {
        // given
        User user = createUser("nicknameuser@example.com", "nickname2");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByNickname("nickname2");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 존재 여부 확인 - 존재하지 않을 때")
    void existsByNickname_false() {
        // when
        boolean exists = userRepository.existsByNickname("nonexistentnickname");

        // then
        assertThat(exists).isFalse();
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password("encodedpassword") // 임시 비밀번호
                .nickname(nickname)
                .role(UserType.USER)
                .profileImageUrl("default-profile-url")
                .build();
    }
}
