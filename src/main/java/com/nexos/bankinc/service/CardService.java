package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.dto.response.RechargeResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CardService {

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    CardResponse generateCardNumber(String productId);

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'CLIENTE')")
    void enrollCard(EnrollCardRequest request);

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    String blockCard(String cardId);

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'CLIENTE')")
    RechargeResponse rechargeBalance(BalanceRequest request);

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'CLIENTE')")
    BalanceResponse getBalance(String cardId);

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    List<CardResponse> findCards();

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    String activeCard(String cardId);
}