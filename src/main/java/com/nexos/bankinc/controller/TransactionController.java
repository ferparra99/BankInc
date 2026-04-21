package com.nexos.bankinc.controller;

import com.nexos.bankinc.dto.request.AnulationRequest;
import com.nexos.bankinc.dto.request.BuyRequest;
import com.nexos.bankinc.dto.response.TransactionResponse;
import com.nexos.bankinc.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 6. Compra
    @PostMapping("/buy")
    public ResponseEntity<TransactionResponse> buy(
            @RequestBody BuyRequest request) {
        return ResponseEntity.ok(transactionService.buy(request));
    }

    // 7. Consultar transacción
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    // 8. Anular transacción
    @PostMapping("/anulation")
    public ResponseEntity<TransactionResponse> annulTransaction(
            @RequestBody AnulationRequest request) {
        return ResponseEntity.ok(transactionService.annulTransaction(request));
    }

    // 9. Obtener todas las transacciones
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}