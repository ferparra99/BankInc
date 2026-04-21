package com.nexos.bankinc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private String cardId;
    private BigDecimal balance;
}