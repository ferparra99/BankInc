package com.nexos.bankinc.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("Valor de compra NO valido");
    }
}