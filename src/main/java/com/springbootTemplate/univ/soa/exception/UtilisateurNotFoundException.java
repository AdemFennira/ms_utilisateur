package com.springbootTemplate.univ.soa.exception;

public class UtilisateurNotFoundException extends RuntimeException {
    public UtilisateurNotFoundException(String message) {
        super(message);
    }
}