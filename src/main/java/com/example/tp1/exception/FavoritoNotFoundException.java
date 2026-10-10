package com.example.tp1.exception;

public class FavoritoNotFoundException extends RuntimeException {

    public FavoritoNotFoundException(String message) {
        super(message);
    }
}