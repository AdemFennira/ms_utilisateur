package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.*;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UtilisateurService {

    UtilisateurResponseDto register(UtilisateurCreateDto createDto);

    String login(LoginDto loginDto);

    UtilisateurResponseDto getUtilisateurById(Long id);

    UtilisateurResponseDto getUtilisateurByEmail(String email);

    List<UtilisateurResponseDto> getAllUtilisateurs();

    UtilisateurResponseDto updateUtilisateur(Long id, UtilisateurUpdateDto updateDto);

    void deleteUtilisateur(Long id);

    // Added methods for connected user
    UtilisateurResponseDto getUtilisateurConnecte(Authentication authentication);

    UtilisateurResponseDto updateUtilisateurConnecte(Authentication authentication, UtilisateurUpdateDto updateDto);

    void deleteUtilisateurConnecte(Authentication authentication);

    // Preferences
    PreferencesDto getPreferences(Authentication authentication);

    PreferencesDto updatePreferences(Authentication authentication, PreferencesDto preferencesDto);

    // Export
    byte[] exportUserData(Authentication authentication);
}