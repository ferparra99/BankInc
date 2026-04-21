package com.nexos.bankinc.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException() {
        super("La tarjeta no ha sido activada");
    }
}