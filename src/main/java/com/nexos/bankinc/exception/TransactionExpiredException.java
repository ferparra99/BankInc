package com.nexos.bankinc.exception;

public class TransactionExpiredException extends RuntimeException {
    public TransactionExpiredException() {
        super("La transacción supera las 24 horas y no puede anularse");
    }
}