package com.nexos.bankinc.controller;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.dto.response.RechargeResponse;
import com.nexos.bankinc.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // 1. Generar número de tarjeta
    @GetMapping("/{productId}/number")
    public ResponseEntity<CardResponse> generateCardNumber(
            @PathVariable int productId) {

        CardResponse responseCard = cardService.generateCardNumber(String.valueOf(productId));
        return ResponseEntity.ok(responseCard);
    }

    // 2. Activar tarjeta
    @PostMapping("/enroll")
    public ResponseEntity<String> enrollCard(@RequestBody EnrollCardRequest request) {
        cardService.enrollCard(request);

        return ResponseEntity.ok("Tarjeta activada con éxito");
    }

    // 3. Bloquear tarjeta
    @DeleteMapping("/{cardId}")
    public ResponseEntity<String> blockCard(@PathVariable String cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Tarjeta bloqueda con éxito");
    }

    // 4. Recargar saldo
    @PostMapping("/balance")
    public ResponseEntity<RechargeResponse> rechargeBalance(@RequestBody BalanceRequest request) {
        RechargeResponse rechargeResponse = cardService.rechargeBalance(request);
        return ResponseEntity.ok(rechargeResponse);
    }

    // 5. Consultar saldo
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }

    // 6. Consultar tarjeta
    @GetMapping("/allClients")
    public ResponseEntity<List<CardResponse>> findCards() {
        return ResponseEntity.ok(cardService.findCards());
    }
}