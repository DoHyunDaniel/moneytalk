package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByProductIdAndReviewerId(Long productId, Long id);

	List<Review> findByProductId(Long productId);

	List<Review> findByTarget(User user);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
	Double findAverageRatingByProductId(@Param("productId") Long productId);

	@Query("SELECT COUNT(r), AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
	List<Object[]> findReviewCountAndAverageRatingByProductId(@Param("productId") Long productId);
	
	long countByProductId(Long productId);

}
