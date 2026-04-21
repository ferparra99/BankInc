package com.nexos.bankinc.dto.request;

import lombok.Data;

@Data
public class AnulationRequest {
    private String cardId;
    private Long transactionId;
}