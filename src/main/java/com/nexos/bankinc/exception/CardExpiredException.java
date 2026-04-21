package com.nexos.bankinc.exception;

public class CardExpiredException extends RuntimeException {
    public CardExpiredException() {
        super("La tarjeta está vencida");
    }
}