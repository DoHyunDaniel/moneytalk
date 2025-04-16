package com.example.moneytalk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


// 통계 관련 DTO
// Repository에서 @Query로 통계 정보를 직접 담아오는 내부 전용 DTO
@Getter
@AllArgsConstructor
public class ReviewStatsDto {
	private Long reviewCount;
	private Double averageRating;
}
