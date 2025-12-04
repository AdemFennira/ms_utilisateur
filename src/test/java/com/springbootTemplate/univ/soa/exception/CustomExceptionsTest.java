package com.springbootTemplate.univ.soa.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void emailAlreadyExistsException_ShouldStoreMessage() {
        String msg = "Email pris";
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException(msg);

        assertEquals(msg, ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void utilisateurNotFoundException_ShouldStoreMessage() {
        String msg = "User inconnu";
        UtilisateurNotFoundException ex = new UtilisateurNotFoundException(msg);

        assertEquals(msg, ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}