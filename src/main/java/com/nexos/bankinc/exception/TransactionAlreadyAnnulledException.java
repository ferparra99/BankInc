package com.nexos.bankinc.exception;

public class TransactionAlreadyAnnulledException extends RuntimeException {
    public TransactionAlreadyAnnulledException() {
        super("La transacción ya fue anulada");
    }
}