package com.springbootTemplate.univ.soa.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValid_thenNoViolations() {
        LoginDto dto = LoginDto.builder()
                .email("admin@univ.fr")
                .motDePasse("secret")
                .build();

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void whenEmailEmpty_thenViolation() {
        LoginDto dto = LoginDto.builder()
                .email("")
                .motDePasse("secret")
                .build();

        assertFalse(validator.validate(dto).isEmpty());
    }
}