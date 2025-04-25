package com.example.moneytalk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.dto.ProductSearchRequestDto;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
	@Query("""
			    SELECT p FROM Product p
			    JOIN FETCH p.user
			    WHERE p.id = :id
			""")
	Optional<Product> findWithUserById(@Param("id") Long id);

	List<Product> findAllByOrderByCreatedAtDesc();

	List<Product> searchByConditions(ProductSearchRequestDto request);
}
