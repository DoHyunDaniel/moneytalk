package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ReviewStatsDto;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByProductIdAndReviewerId(Long productId, Long id);

	List<Review> findByProductId(Long productId);

	List<Review> findByReviewee(User user);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
	Double findAverageRatingByProductId(@Param("productId") Long productId);

	@Query("SELECT new com.example.moneytalk.dto.ReviewStatsDto(COUNT(r), COALESCE(AVG(r.rating), 0.0)) " +
		       "FROM Review r WHERE r.product.id = :productId")
		ReviewStatsDto findReviewStatsByProductId(@Param("productId") Long productId);
	
	long countByProductId(Long productId);

}
