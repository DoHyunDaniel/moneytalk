package com.example.moneytalk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}
