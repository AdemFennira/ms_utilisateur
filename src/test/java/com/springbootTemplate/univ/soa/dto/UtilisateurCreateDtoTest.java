package com.springbootTemplate.univ.soa.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilisateurCreateDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        UtilisateurCreateDto dto = UtilisateurCreateDto.builder()
                .email("test@univ.fr")
                .motDePasse("password123") // > 8 chars
                .nom("Dupont")
                .prenom("Jean")
                .build();

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmailInvalid_thenViolation() {
        UtilisateurCreateDto dto = UtilisateurCreateDto.builder()
                .email("not-an-email")
                .motDePasse("password123")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("valide")));
    }

    @Test
    void whenPasswordTooShort_thenViolation() {
        UtilisateurCreateDto dto = UtilisateurCreateDto.builder()
                .email("test@univ.fr")
                .motDePasse("123") // < 8 chars
                .nom("Dupont")
                .prenom("Jean")
                .build();

        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("8 caractères")));
    }

    @Test
    void whenMandatoryFieldsMissing_thenViolations() {
        UtilisateurCreateDto dto = new UtilisateurCreateDto(); // Tout est null

        var violations = validator.validate(dto);
        // On attend 4 erreurs : email, motDePasse, nom, prenom
        assertFalse(violations.isEmpty());
    }
}