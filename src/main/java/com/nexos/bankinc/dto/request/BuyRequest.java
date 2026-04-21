package com.nexos.bankinc.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuyRequest {
    private String cardId;
    private BigDecimal price;
}