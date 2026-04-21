package com.nexos.bankinc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_id", length = 16, nullable = false)
    private String cardId;

    @Column(name = "product_id", length = 6, nullable = false)
    private String productId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean blocked;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.balance = BigDecimal.ZERO;
        this.active = false;
        this.blocked = false;
        this.expirationDate = LocalDate.now().plusYears(3);
    }
}