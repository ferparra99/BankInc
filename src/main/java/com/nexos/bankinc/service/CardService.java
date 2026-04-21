package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.dto.response.RechargeResponse;
import com.nexos.bankinc.entity.Card;

import java.util.List;
import java.util.Optional;

public interface CardService {

    CardResponse generateCardNumber(String productId);
    void enrollCard(EnrollCardRequest request);
    void blockCard(String cardId);
    RechargeResponse rechargeBalance(BalanceRequest request);
    BalanceResponse getBalance(String cardId);
    List<CardResponse> findCards();
}