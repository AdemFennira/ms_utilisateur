package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.*;

import java.util.List;

public interface UtilisateurService {

    UtilisateurResponseDto register(UtilisateurCreateDto createDto);

    String login(LoginDto loginDto);

    UtilisateurResponseDto getUtilisateurById(Long id);

    UtilisateurResponseDto getUtilisateurByEmail(String email);

    List<UtilisateurResponseDto> getAllUtilisateurs();

    UtilisateurResponseDto updateUtilisateur(Long id, UtilisateurUpdateDto updateDto);

    void deleteUtilisateur(Long id);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}