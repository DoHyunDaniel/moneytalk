package com.example.moneytalk.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.moneytalk.type.LedgerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ledgers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ledger {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type; // INCOME or EXPENSE

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(length = 20)
    private String paymentMethod; // 예: 카드, 현금 등 (선택사항)

    @Column(length = 100)
    private String tag; // 예: "식비/카페/스타벅스" (카테고리 구체화 or 태깅용)

}
