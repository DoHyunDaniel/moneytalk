package com.example.moneytalk.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.moneytalk.type.PurchaseType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseType type;  // PURCHASE or SALE

    @CreationTimestamp
    private LocalDateTime createdAt;
}
