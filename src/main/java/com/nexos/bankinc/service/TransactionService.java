package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.AnulationRequest;
import com.nexos.bankinc.dto.request.BuyRequest;
import com.nexos.bankinc.dto.response.TransactionResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TransactionService {
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'CLIENTE')")
    TransactionResponse buy(BuyRequest request);

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'CLIENTE')")
    TransactionResponse getTransaction(Long transactionId);

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    TransactionResponse annulTransaction(AnulationRequest request);

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    List<TransactionResponse> getAllTransactions();
}