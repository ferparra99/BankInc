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

    // 7. Compra
    @PostMapping("/buy")
    public ResponseEntity<TransactionResponse> buy(
            @RequestBody BuyRequest request) {
        return ResponseEntity.ok(transactionService.buy(request));
    }

    // 8. Consultar transacción
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    // 9. Anular transacción
    @PostMapping("/anulation")
    public ResponseEntity<TransactionResponse> annulTransaction(
            @RequestBody AnulationRequest request) {
        return ResponseEntity.ok(transactionService.annulTransaction(request));
    }

    // 10. Obtener todas las transacciones
    @GetMapping("/transaccions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}