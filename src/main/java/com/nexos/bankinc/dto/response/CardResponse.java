package com.nexos.bankinc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CardResponse {
    private String cardId;
    private String productId;
    private String clientName;
    private LocalDate expirationDate;
    private BigDecimal balance;
    private boolean active;
    private boolean blocked;
    private LocalDateTime createdAt;

}