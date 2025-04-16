package com.example.moneytalk.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.moneytalk.domain.Product;
import com.example.moneytalk.dto.ProductSearchRequestDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final EntityManager em;

	@Override
	public List<Product> searchByConditions(ProductSearchRequestDto request) {
		StringBuilder sb = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

		if (request.getKeyword() != null) {
			sb.append(" AND (p.title LIKE :kw OR p.description LIKE :kw)");
		}
		if (request.getCategory() != null) {
			sb.append(" AND p.category = :category");
		}
		if (request.getLocation() != null) {
			sb.append(" AND p.location = :location");
		}
		if (request.getMinPrice() != null) {
			sb.append(" AND p.price >= :minPrice");
		}
		if (request.getMaxPrice() != null) {
			sb.append(" AND p.price <= :maxPrice");
		}
		if (request.getStatus() != null) {
			sb.append(" AND p.status = :status");
		}

		// 정렬 조건
		if ("price_asc".equals(request.getSort())) {
			sb.append(" ORDER BY p.price ASC");
		} else if ("price_desc".equals(request.getSort())) {
			sb.append(" ORDER BY p.price DESC");
		} else {
			sb.append(" ORDER BY p.createdAt DESC");
		}

		TypedQuery<Product> query = em.createQuery(sb.toString(), Product.class);

		if (request.getKeyword() != null) {
			query.setParameter("kw", "%" + request.getKeyword() + "%");
		}
		if (request.getCategory() != null) {
			query.setParameter("category", request.getCategory());
		}
		if (request.getLocation() != null) {
			query.setParameter("location", request.getLocation());
		}
		if (request.getMinPrice() != null) {
			query.setParameter("minPrice", request.getMinPrice());
		}
		if (request.getMaxPrice() != null) {
			query.setParameter("maxPrice", request.getMaxPrice());
		}
		if (request.getStatus() != null) {
			query.setParameter("status", request.getStatus());
		}

		return query.getResultList();
	}
}
