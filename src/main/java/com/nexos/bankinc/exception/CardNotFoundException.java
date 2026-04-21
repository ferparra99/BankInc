package com.nexos.bankinc.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String cardId) {
        super("Tarjeta no encontrada: " + cardId);
    }
}
