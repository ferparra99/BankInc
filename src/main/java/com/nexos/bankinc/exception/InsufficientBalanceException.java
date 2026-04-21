package com.nexos.bankinc.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Saldo insuficiente para realizar la compra");
    }
}