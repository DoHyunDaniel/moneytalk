package com.example.moneytalk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.moneytalk.domain.ChatRoom;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@Query("""
			    SELECT r FROM ChatRoom r
			    WHERE r.buyer = :user OR r.seller = :user
			    ORDER BY r.lastMessageAt DESC
			""")
	List<ChatRoom> findAllByUser(@Param("user") User user);


    // 사용자가 구매자 또는 판매자인 모든 채팅방을 최근 메시지 기준으로 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.buyer = :user OR cr.seller = :user
        ORDER BY cr.lastMessageAt DESC
    """)
    List<ChatRoom> findAllByUserOrderByLastMessageAtDesc(User user);
    
    // 특정 상품 + 구매자 + 판매자 조합의 방이 있는지 확인
    Optional<ChatRoom> findByProductAndBuyerAndSeller(Product product, User buyer, User seller);
	List<ChatRoom> findByBuyerOrSellerOrderByLastMessageAtDesc(User loginUser, User loginUser2);
	
	
}
