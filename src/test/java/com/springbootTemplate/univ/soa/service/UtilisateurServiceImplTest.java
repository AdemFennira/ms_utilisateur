package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.client.PersistanceClient;
import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.exception.EmailAlreadyExistsException;
import com.springbootTemplate.univ.soa.exception.UtilisateurNotFoundException;
import com.springbootTemplate.univ.soa.model.Role;
import com.springbootTemplate.univ.soa.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private PersistanceClient persistanceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private UtilisateurCreateDto createDto;
    private UtilisateurResponseDto responseDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        createDto = UtilisateurCreateDto.builder()
                .email("test@univ.fr")
                .motDePasse("rawPassword")
                .nom("Doe")
                .prenom("John")
                .build();

        responseDto = UtilisateurResponseDto.builder()
                .id(1L)
                .email("test@univ.fr")
                .role(Role.USER)
                .actif(true)
                .build();

        loginDto = LoginDto.builder()
                .email("test@univ.fr")
                .motDePasse("rawPassword")
                .build();
    }

    // --- TESTS REGISTER ---

    @Test
    void register_Success() {
        // GIVEN
        when(persistanceClient.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(persistanceClient.createUtilisateur(any(UtilisateurCreateDto.class))).thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.register(createDto);

        // THEN
        assertNotNull(result);
        assertEquals("test@univ.fr", result.getEmail());

        // Vérifie qu'on a bien encodé le mot de passe avant l'envoi
        verify(passwordEncoder).encode("rawPassword");
        verify(persistanceClient).createUtilisateur(argThat(dto -> dto.getMotDePasse().equals("encodedPassword")));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(persistanceClient.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> utilisateurService.register(createDto));

        verify(persistanceClient, never()).createUtilisateur(any());
    }

    // --- TESTS LOGIN ---

    @Test
    void login_Success() {
        // GIVEN
        when(persistanceClient.getUtilisateurByEmail(loginDto.getEmail())).thenReturn(responseDto);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // WHEN
        String token = utilisateurService.login(loginDto);

        // THEN
        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(jwtUtil).generateToken("test@univ.fr", "USER");
    }

    @Test
    void login_UserNotFound_ThrowsBadCredentials() {
        // Simulation d'une 404 du client feign transformée en exception
        when(persistanceClient.getUtilisateurByEmail(anyString()))
                .thenThrow(new UtilisateurNotFoundException("Introuvable"));

        // Doit renvoyer BadCredentials pour raison de sécurité (ne pas dire si l'email existe)
        assertThrows(BadCredentialsException.class, () -> utilisateurService.login(loginDto));
    }

    @Test
    void login_AccountInactive_ThrowsBadCredentials() {
        responseDto.setActif(false);
        when(persistanceClient.getUtilisateurByEmail(loginDto.getEmail())).thenReturn(responseDto);

        Exception ex = assertThrows(BadCredentialsException.class, () -> utilisateurService.login(loginDto));
        assertEquals("Compte désactivé", ex.getMessage());
    }

    // --- TESTS UPDATE ---

    @Test
    void updateUtilisateur_WithPasswordChange() {
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .nouveauMotDePasse("newPass")
                .build();

        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(persistanceClient.updateUtilisateur(eq(1L), any(UtilisateurUpdateDto.class))).thenReturn(responseDto);

        utilisateurService.updateUtilisateur(1L, updateDto);

        // Vérifie que le mot de passe a été encodé
        verify(passwordEncoder).encode("newPass");
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto -> dto.getNouveauMotDePasse().equals("encodedNewPass")));
    }

    @Test
    void updateUtilisateur_NoPasswordChange() {
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder().nom("NewName").build();

        when(persistanceClient.updateUtilisateur(eq(1L), any())).thenReturn(responseDto);

        utilisateurService.updateUtilisateur(1L, updateDto);

        // Vérifie que l'encodeur n'est PAS appelé
        verify(passwordEncoder, never()).encode(anyString());
        verify(persistanceClient).updateUtilisateur(eq(1L), any());
    }
}