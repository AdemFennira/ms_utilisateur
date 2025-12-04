package com.springbootTemplate.univ.soa.exception;

import com.springbootTemplate.univ.soa.security.JwtAuthenticationFilter;
import com.springbootTemplate.univ.soa.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandler.class
})
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // --- CONTROLLER FICTIF ---
    @RestController
    public static class TestController {

        @GetMapping("/test/user-not-found")
        public void throwUserNotFound() {
            throw new UtilisateurNotFoundException("Utilisateur introuvable avec ID 1");
        }

        @GetMapping("/test/email-exists")
        public void throwEmailExists() {
            throw new EmailAlreadyExistsException("Cet email est déjà pris");
        }

        @GetMapping("/test/bad-credentials")
        public void throwBadCredentials() {
            throw new BadCredentialsException("Mot de passe incorrect");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Accès interdit");
        }

        @GetMapping("/test/generic-error")
        public void throwGeneric() {
            throw new RuntimeException("Oups, crash système");
        }

        @PostMapping("/test/validation")
        public void validate(@RequestBody @Valid TestDto dto) {
        }
    }

    @Data
    static class TestDto {
        @NotNull(message = "Champ obligatoire")
        private String field;
    }

    // --- TESTS ---

    @Test
    void handleUtilisateurNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void handleEmailAlreadyExists_ShouldReturn409() throws Exception {
        mockMvc.perform(get("/test/email-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void handleBadCredentials_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void handleAccessDenied_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void handleValidationErrors_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleGlobalException_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}