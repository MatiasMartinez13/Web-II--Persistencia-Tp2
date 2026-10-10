package com.example.tp1.exception;

public class ListaNotFoundException extends RuntimeException {

    public ListaNotFoundException(String message) {
        super(message);
    }
}