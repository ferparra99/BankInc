package com.nexos.bankinc.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long transactionId) {
        super("Transacción no encontrada: " + transactionId);
    }
}