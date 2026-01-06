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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private UtilisateurAuthDto authDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        createDto = UtilisateurCreateDto.builder()
                .email("test@univ.fr")
                .motDePasse("rawPassword")
                .nom("Doe")
                .prenom("John")
                // ✅ NOUVELLES PRÉFÉRENCES
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L, 5L))
                .build();

        responseDto = UtilisateurResponseDto.builder()
                .id(1L)
                .email("test@univ.fr")
                .nom("Doe")
                .prenom("John")
                .role(Role.USER)
                .actif(true)
                // ✅ NOUVELLES PRÉFÉRENCES
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L, 5L))
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        authDto = UtilisateurAuthDto.builder()
                .id(1L)
                .email("test@univ.fr")
                .motDePasse("$2a$10$hashedPassword") // Hash BCrypt simulé
                .nom("Doe")
                .prenom("John")
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
        when(persistanceClient.createUtilisateur(any(UtilisateurCreateDto.class))).thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.register(createDto);

        // THEN
        assertNotNull(result);
        assertEquals("test@univ.fr", result.getEmail());

        // ✅ Le mot de passe n'est PLUS encodé dans MS-UTILISATEUR (c'est MS-PERSISTANCE qui le fait)
        verify(passwordEncoder, never()).encode(anyString());
        verify(persistanceClient).createUtilisateur(argThat(dto ->
                dto.getEmail().equals("test@univ.fr") &&
                        dto.getMotDePasse().equals("rawPassword")
        ));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        // GIVEN
        when(persistanceClient.existsByEmail(createDto.getEmail())).thenReturn(true);

        // WHEN & THEN
        assertThrows(EmailAlreadyExistsException.class, () -> utilisateurService.register(createDto));
        verify(persistanceClient, never()).createUtilisateur(any());
    }

    // --- TESTS LOGIN ---

    @Test
    void login_Success() {
        // GIVEN
        when(persistanceClient.getUtilisateurForAuth(loginDto.getEmail())).thenReturn(authDto);
        when(passwordEncoder.matches("rawPassword", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // WHEN
        String token = utilisateurService.login(loginDto);

        // THEN
        assertNotNull(token);
        assertEquals("mock-jwt-token", token);
        verify(passwordEncoder).matches("rawPassword", "$2a$10$hashedPassword");
        verify(jwtUtil).generateToken("test@univ.fr", "USER");
    }

    @Test
    void login_UserNotFound_ThrowsBadCredentials() {
        // GIVEN
        when(persistanceClient.getUtilisateurForAuth(anyString()))
                .thenThrow(new UtilisateurNotFoundException("Introuvable"));

        // WHEN & THEN
        Exception ex = assertThrows(BadCredentialsException.class, () -> utilisateurService.login(loginDto));
        assertEquals("Email ou mot de passe incorrect", ex.getMessage());
    }

    @Test
    void login_AccountInactive_ThrowsBadCredentials() {
        // GIVEN
        authDto.setActif(false);
        when(persistanceClient.getUtilisateurForAuth(loginDto.getEmail())).thenReturn(authDto);

        // WHEN & THEN
        Exception ex = assertThrows(BadCredentialsException.class, () -> utilisateurService.login(loginDto));
        assertEquals("Compte désactivé", ex.getMessage());
    }

    @Test
    void login_WrongPassword_ThrowsBadCredentials() {
        // GIVEN
        when(persistanceClient.getUtilisateurForAuth(loginDto.getEmail())).thenReturn(authDto);
        when(passwordEncoder.matches("rawPassword", "$2a$10$hashedPassword")).thenReturn(false);

        // WHEN & THEN
        Exception ex = assertThrows(BadCredentialsException.class, () -> utilisateurService.login(loginDto));
        assertEquals("Email ou mot de passe incorrect", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    // --- TESTS UPDATE ---

    @Test
    void updateUtilisateur_WithPasswordChange_Success() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .ancienMotDePasse("rawPassword")
                .nouveauMotDePasse("newPassword123")
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.getUtilisateurForAuth("test@univ.fr")).thenReturn(authDto);
        when(passwordEncoder.matches("rawPassword", "$2a$10$hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newHashedPassword");
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(passwordEncoder).matches("rawPassword", "$2a$10$hashedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getMotDePasse().equals("$2a$10$newHashedPassword")
        ));
    }

    @Test
    void updateUtilisateur_WithPasswordChange_WrongOldPassword_ThrowsException() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .ancienMotDePasse("wrongOldPassword")
                .nouveauMotDePasse("newPassword123")
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.getUtilisateurForAuth("test@univ.fr")).thenReturn(authDto);
        when(passwordEncoder.matches("wrongOldPassword", "$2a$10$hashedPassword")).thenReturn(false);

        // WHEN & THEN
        Exception ex = assertThrows(BadCredentialsException.class,
                () -> utilisateurService.updateUtilisateur(1L, updateDto));
        assertEquals("L'ancien mot de passe est incorrect", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(persistanceClient, never()).updateUtilisateur(anyLong(), any());
    }

    @Test
    void updateUtilisateur_WithPasswordChange_NoOldPassword_ThrowsException() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .nouveauMotDePasse("newPassword123")
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);

        // WHEN & THEN
        Exception ex = assertThrows(BadCredentialsException.class,
                () -> utilisateurService.updateUtilisateur(1L, updateDto));
        assertEquals("L'ancien mot de passe est requis pour changer le mot de passe", ex.getMessage());
        verify(persistanceClient, never()).updateUtilisateur(anyLong(), any());
    }

    @Test
    void updateUtilisateur_OnlyNameChange_Success() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .nom("NewName")
                .prenom("NewFirstName")
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getNom().equals("NewName") &&
                        dto.getPrenom().equals("NewFirstName") &&
                        dto.getMotDePasse() == null
        ));
    }

    @Test
    void updateUtilisateur_ChangeEmail_Success() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .email("newemail@univ.fr")
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getEmail().equals("newemail@univ.fr")
        ));
    }

    // ✅ NOUVEAUX TESTS POUR LES PRÉFÉRENCES ALIMENTAIRES

    @Test
    void updateUtilisateur_ChangeRegimes_Success() {
        // GIVEN
        Set<Long> newRegimes = Set.of(1L, 2L); // Végétarien + Vegan
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .regimesIds(newRegimes)
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getRegimesIds().equals(newRegimes)
        ));
    }

    @Test
    void updateUtilisateur_ChangeAllergenes_Success() {
        // GIVEN
        Set<Long> newAllergenes = Set.of(7L, 8L); // Lait + Fruits à coque
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .allergenesIds(newAllergenes)
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getAllergenesIds().equals(newAllergenes)
        ));
    }

    @Test
    void updateUtilisateur_ChangeTypesCuisine_Success() {
        // GIVEN
        Set<Long> newTypesCuisine = Set.of(2L, 3L, 5L); // Italien + Asiatique + Japonais
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .typesCuisinePreferesIds(newTypesCuisine)
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getTypesCuisinePreferesIds().equals(newTypesCuisine)
        ));
    }

    @Test
    void updateUtilisateur_RemoveAllPreferences_Success() {
        // GIVEN
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .regimesIds(new HashSet<>())
                .allergenesIds(new HashSet<>())
                .typesCuisinePreferesIds(new HashSet<>())
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getRegimesIds().isEmpty() &&
                        dto.getAllergenesIds().isEmpty() &&
                        dto.getTypesCuisinePreferesIds().isEmpty()
        ));
    }

    @Test
    void updateUtilisateur_CompleteUpdate_Success() {
        // GIVEN
        Set<Long> newRegimes = Set.of(1L, 2L);
        Set<Long> newAllergenes = Set.of(7L, 8L);
        Set<Long> newTypesCuisine = Set.of(2L, 3L, 5L);

        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .email("newemail@univ.fr")
                .nom("NewName")
                .prenom("NewFirstName")
                .ancienMotDePasse("rawPassword")
                .nouveauMotDePasse("newPassword123")
                .regimesIds(newRegimes)
                .allergenesIds(newAllergenes)
                .typesCuisinePreferesIds(newTypesCuisine)
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.getUtilisateurForAuth("test@univ.fr")).thenReturn(authDto);
        when(passwordEncoder.matches("rawPassword", "$2a$10$hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newHashedPassword");
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getEmail().equals("newemail@univ.fr") &&
                        dto.getNom().equals("NewName") &&
                        dto.getPrenom().equals("NewFirstName") &&
                        dto.getMotDePasse().equals("$2a$10$newHashedPassword") &&
                        dto.getRegimesIds().equals(newRegimes) &&
                        dto.getAllergenesIds().equals(newAllergenes) &&
                        dto.getTypesCuisinePreferesIds().equals(newTypesCuisine)
        ));
    }

    @Test
    void updateUtilisateur_PartialPreferencesUpdate_Success() {
        // GIVEN - Mise à jour uniquement des régimes, le reste reste inchangé
        Set<Long> newRegimes = Set.of(1L, 2L, 3L);
        UtilisateurUpdateDto updateDto = UtilisateurUpdateDto.builder()
                .regimesIds(newRegimes)
                // allergenesIds et typesCuisinePreferesIds sont null = pas de changement
                .build();

        when(persistanceClient.getUtilisateurById(1L)).thenReturn(responseDto);
        when(persistanceClient.updateUtilisateur(eq(1L), any(MsPersistanceUtilisateurDto.class)))
                .thenReturn(responseDto);

        // WHEN
        UtilisateurResponseDto result = utilisateurService.updateUtilisateur(1L, updateDto);

        // THEN
        assertNotNull(result);
        verify(persistanceClient).updateUtilisateur(eq(1L), argThat(dto ->
                dto.getRegimesIds().equals(newRegimes) &&
                        // Les valeurs existantes sont conservées
                        dto.getAllergenesIds().equals(responseDto.getAllergenesIds()) &&
                        dto.getTypesCuisinePreferesIds().equals(responseDto.getTypesCuisinePreferesIds())
        ));
    }
}