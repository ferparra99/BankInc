package com.nexos.bankinc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
public class RechargeResponse {
    private String message;
    private String cardId;
    private BigDecimal balance;
}
