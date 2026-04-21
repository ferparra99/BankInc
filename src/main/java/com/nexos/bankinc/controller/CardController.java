package com.nexos.bankinc.controller;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // 1. Generar número de tarjeta
    @GetMapping("/{productId}/number")
    public ResponseEntity<Map<String, Object>> generateCardNumber(
            @PathVariable int productId) {

        Map<String, Object> data = new HashMap<>();
        CardResponse responseCard = cardService.generateCardNumber(String.valueOf(productId));
        data.put("cardId", responseCard.getCardId());
        data.put("productId", responseCard.getProductId());
        data.put("balance", responseCard.getBalance());
        data.put("clientName", responseCard.getClientName());
        data.put("createdAt", responseCard.getCreatedAt());
        data.put("active", responseCard.isActive());
        data.put("blocked", responseCard.isBlocked());
        data.put("expirationDate", responseCard.getExpirationDate());

        return ResponseEntity.ok(data);
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
    public ResponseEntity<Void> rechargeBalance(@RequestBody BalanceRequest request) {
        cardService.rechargeBalance(request);
        return ResponseEntity.ok().build();
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