package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;

public interface ReviewRepository extends JpaRepository<Review, Long>{

	boolean existsByProductIdAndReviewerId(Long productId, Long id);

	List<Review> findByProductId(Long productId);

	List<Review> findByTarget(User user);
	
	Double findAverageRatingByProductId(Long productId);
	
	long countByProductId(Long productId);

}
