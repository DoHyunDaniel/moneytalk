package com.example.moneytalk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 특정 상품 + 구매자 + 판매자 조합의 방이 있는지 확인
    Optional<ChatRoom> findByProductAndBuyerAndSeller(Product product, User buyer, User seller);
}
