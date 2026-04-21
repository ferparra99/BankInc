package com.nexos.bankinc.exception;

public class CardBlockedException extends RuntimeException {
    public CardBlockedException() {
        super("La tarjeta está bloqueada");
    }
}