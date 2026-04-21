package com.nexos.bankinc.dto.response;

import com.nexos.bankinc.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private String cardId;
    private BigDecimal price;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
}