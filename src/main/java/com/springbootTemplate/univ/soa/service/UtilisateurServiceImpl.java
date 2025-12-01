package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.client.PersistanceClient;
import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.exception.EmailAlreadyExistsException;
import com.springbootTemplate.univ.soa.exception.UtilisateurNotFoundException;
import com.springbootTemplate.univ.soa.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final PersistanceClient persistanceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UtilisateurResponseDto register(UtilisateurCreateDto createDto) {
        log.info("📝 Tentative d'inscription - Email: {}", createDto.getEmail());

        // Vérifier si l'email existe déjà via ms-persistance
        if (persistanceClient.existsByEmail(createDto.getEmail())) {
            log.error("❌ Email déjà utilisé: {}", createDto.getEmail());
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Hasher le mot de passe avant de l'envoyer à ms-persistance
        createDto.setMotDePasse(passwordEncoder.encode(createDto.getMotDePasse()));

        // Créer l'utilisateur via ms-persistance
        UtilisateurResponseDto response = persistanceClient.createUtilisateur(createDto);

        log.info("✅ Utilisateur créé avec succès - ID: {}, Email: {}",
                response.getId(), response.getEmail());

        return response;
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("🔐 Tentative de connexion - Email: {}", loginDto.getEmail());

        // Récupérer l'utilisateur via ms-persistance
        UtilisateurResponseDto utilisateur;
        try {
            utilisateur = persistanceClient.getUtilisateurByEmail(loginDto.getEmail());
        } catch (UtilisateurNotFoundException e) {
            log.error("❌ Utilisateur non trouvé: {}", loginDto.getEmail());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Vérifier si le compte est actif
        if (!utilisateur.getActif()) {
            log.error("❌ Compte désactivé: {}", loginDto.getEmail());
            throw new BadCredentialsException("Compte désactivé");
        }

        // ⚠️ PROBLÈME : ms-persistance ne retourne pas le mot de passe hashé
        // SOLUTION TEMPORAIRE : Il faut que ms-persistance expose un endpoint
        // de validation des credentials ou retourne le hash dans un endpoint sécurisé

        // Pour l'instant, on génère le token si l'utilisateur existe et est actif
        // TODO: Implémenter la validation du mot de passe via ms-persistance

        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());
        log.info("✅ Connexion réussie - Email: {}", utilisateur.getEmail());

        return token;
    }

    @Override
    public UtilisateurResponseDto getUtilisateurById(Long id) {
        log.info("🔍 Recherche utilisateur par ID: {}", id);
        return persistanceClient.getUtilisateurById(id);
    }

    @Override
    public UtilisateurResponseDto getUtilisateurByEmail(String email) {
        log.info("🔍 Recherche utilisateur par email: {}", email);
        return persistanceClient.getUtilisateurByEmail(email);
    }

    @Override
    public List<UtilisateurResponseDto> getAllUtilisateurs() {
        log.info("📋 Récupération de tous les utilisateurs");
        List<UtilisateurResponseDto> utilisateurs = persistanceClient.getAllUtilisateurs();
        log.info("✅ {} utilisateurs trouvés", utilisateurs.size());
        return utilisateurs;
    }

    @Override
    public UtilisateurResponseDto updateUtilisateur(Long id, UtilisateurUpdateDto updateDto) {
        log.info("📝 Mise à jour utilisateur - ID: {}", id);

        // Si un nouveau mot de passe est fourni, le hasher
        if (updateDto.getNouveauMotDePasse() != null) {
            updateDto.setNouveauMotDePasse(passwordEncoder.encode(updateDto.getNouveauMotDePasse()));
            log.info("🔐 Mot de passe mis à jour pour l'utilisateur ID: {}", id);
        }

        UtilisateurResponseDto response = persistanceClient.updateUtilisateur(id, updateDto);
        log.info("✅ Utilisateur mis à jour avec succès - ID: {}", id);

        return response;
    }

    @Override
    public void deleteUtilisateur(Long id) {
        log.info("🗑️ Suppression utilisateur - ID: {}", id);
        persistanceClient.deleteUtilisateur(id);
        log.info("✅ Utilisateur supprimé avec succès - ID: {}", id);
    }
}