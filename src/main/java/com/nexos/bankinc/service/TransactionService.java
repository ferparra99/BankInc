package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.AnulationRequest;
import com.nexos.bankinc.dto.request.BuyRequest;
import com.nexos.bankinc.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse buy(BuyRequest request);
    TransactionResponse getTransaction(Long transactionId);
    TransactionResponse annulTransaction(AnulationRequest request);
    List<TransactionResponse> getAllTransactions();
}