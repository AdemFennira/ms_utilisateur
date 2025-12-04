package com.springbootTemplate.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.model.Role;
import com.springbootTemplate.univ.soa.security.JwtAuthenticationFilter;
import com.springbootTemplate.univ.soa.security.JwtUtil;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilisateurController.class)
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité à l'exécution
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UtilisateurService utilisateurService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UtilisateurResponseDto utilisateurResponse;
    private UtilisateurCreateDto createDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        utilisateurResponse = UtilisateurResponseDto.builder()
                .id(1L)
                .email("test@univ.fr")
                .nom("Doe")
                .prenom("John")
                .role(Role.USER)
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .build();

        createDto = UtilisateurCreateDto.builder()
                .email("test@univ.fr")
                .motDePasse("password123")
                .nom("Doe")
                .prenom("John")
                .build();

        loginDto = LoginDto.builder()
                .email("test@univ.fr")
                .motDePasse("password123")
                .build();
    }

    @Test
    void register_ShouldReturnCreated() throws Exception {
        when(utilisateurService.register(any(UtilisateurCreateDto.class))).thenReturn(utilisateurResponse);

        mockMvc.perform(post("/api/utilisateurs/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@univ.fr"));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.test.XYZ";
        when(utilisateurService.login(any(LoginDto.class))).thenReturn(fakeToken);

        mockMvc.perform(post("/api/utilisateurs/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken));
    }

    @Test
    void getUtilisateurById_ShouldReturnUser() throws Exception {
        when(utilisateurService.getUtilisateurById(1L)).thenReturn(utilisateurResponse);

        mockMvc.perform(get("/api/utilisateurs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    void getAllUtilisateurs_ShouldReturnList() throws Exception {
        List<UtilisateurResponseDto> list = Collections.singletonList(utilisateurResponse);
        when(utilisateurService.getAllUtilisateurs()).thenReturn(list);

        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void updateUtilisateur_ShouldReturnUpdatedUser() throws Exception {
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder().nom("Smith").build();
        UtilisateurResponseDto updatedResponse = UtilisateurResponseDto.builder().id(1L).nom("Smith").build();

        when(utilisateurService.updateUtilisateur(eq(1L), any(UtilisateurUpdateDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/utilisateurs/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Smith"));
    }

    @Test
    void deleteUtilisateur_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(utilisateurService).deleteUtilisateur(1L);

        mockMvc.perform(delete("/api/utilisateurs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilisateur supprimé avec succès"));
    }

    @Test
    void register_InvalidInput_ShouldReturnBadRequest() throws Exception {
        createDto.setEmail("invalid-email");
        createDto.setMotDePasse("123");

        mockMvc.perform(post("/api/utilisateurs/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }
}