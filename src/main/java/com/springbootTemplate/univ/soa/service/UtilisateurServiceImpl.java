package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.client.PersistanceClient;
import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.exception.EmailAlreadyExistsException;
import com.springbootTemplate.univ.soa.exception.UtilisateurNotFoundException;
import com.springbootTemplate.univ.soa.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        UtilisateurResponseDto response = persistanceClient.createUtilisateur(createDto);

        log.info("✅ Utilisateur créé avec succès - ID: {}, Email: {}",
                response.getId(), response.getEmail());

        return response;
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("🔐 Tentative de connexion - Email: {}", loginDto.getEmail());

        // Récupérer l'utilisateur avec son hash via ms-persistance
        UtilisateurAuthDto utilisateur;
        try {
            utilisateur = persistanceClient.getUtilisateurForAuth(loginDto.getEmail());
        } catch (UtilisateurNotFoundException e) {
            log.error("❌ Utilisateur non trouvé: {}", loginDto.getEmail());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Vérifier si le compte est actif
        if (!utilisateur.getActif()) {
            log.error("❌ Compte désactivé: {}", loginDto.getEmail());
            throw new BadCredentialsException("Compte désactivé");
        }

        // ✅ VALIDATION DU MOT DE PASSE avec le PasswordEncoder de MS-UTILISATEUR
        if (!passwordEncoder.matches(loginDto.getMotDePasse(), utilisateur.getMotDePasse())) {
            log.error("❌ Mot de passe incorrect pour: {}", loginDto.getEmail());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Générer le token JWT
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
        UtilisateurResponseDto existingUser = persistanceClient.getUtilisateurById(id);

        if (updateDto.getNouveauMotDePasse() != null && !updateDto.getNouveauMotDePasse().trim().isEmpty()) {

            if (updateDto.getAncienMotDePasse() == null || updateDto.getAncienMotDePasse().trim().isEmpty()) {
                log.error("❌ Ancien mot de passe requis pour changer le mot de passe");
                throw new BadCredentialsException("L'ancien mot de passe est requis pour changer le mot de passe");
            }
            UtilisateurAuthDto authUser = persistanceClient.getUtilisateurForAuth(existingUser.getEmail());

            if (!passwordEncoder.matches(updateDto.getAncienMotDePasse(), authUser.getMotDePasse())) {
                log.error("❌ Ancien mot de passe incorrect pour l'utilisateur ID: {}", id);
                throw new BadCredentialsException("L'ancien mot de passe est incorrect");
            }

            updateDto.setNouveauMotDePasse(passwordEncoder.encode(updateDto.getNouveauMotDePasse()));
            log.info("🔐 Mot de passe mis à jour pour l'utilisateur ID: {}", id);
        }

        MsPersistanceUtilisateurDto fullDto = MsPersistanceUtilisateurDto.builder()
                .id(existingUser.getId())
                .email(updateDto.getEmail() != null ? updateDto.getEmail() : existingUser.getEmail())
                .nom(updateDto.getNom() != null ? updateDto.getNom() : existingUser.getNom())
                .prenom(updateDto.getPrenom() != null ? updateDto.getPrenom() : existingUser.getPrenom())
                .motDePasse(updateDto.getNouveauMotDePasse()) // null = pas de changement
                .role(existingUser.getRole())
                .actif(existingUser.getActif())
                // ✅ NOUVELLES PRÉFÉRENCES ALIMENTAIRES
                .regimesIds(updateDto.getRegimesIds() != null ?
                        updateDto.getRegimesIds() : existingUser.getRegimesIds())
                .allergenesIds(updateDto.getAllergenesIds() != null ?
                        updateDto.getAllergenesIds() : existingUser.getAllergenesIds())
                .typesCuisinePreferesIds(updateDto.getTypesCuisinePreferesIds() != null ?
                        updateDto.getTypesCuisinePreferesIds() : existingUser.getTypesCuisinePreferesIds())
                .dateCreation(existingUser.getDateCreation())
                .dateModification(existingUser.getDateModification())
                .build();

        UtilisateurResponseDto response = persistanceClient.updateUtilisateur(id, fullDto);
        log.info("✅ Utilisateur mis à jour avec succès - ID: {}", id);

        return response;
    }

    @Override
    public void deleteUtilisateur(Long id) {
        log.info("🗑️ Suppression utilisateur - ID: {}", id);
        persistanceClient.deleteUtilisateur(id);
        log.info("✅ Utilisateur supprimé avec succès - ID: {}", id);
    }

    @Autowired
    private EmailService emailService;

    @Override
    public void forgotPassword(String email) {
        log.info("🔑 Demande de réinitialisation de mot de passe pour: {}", email);

        try {
            // 1. Récupérer l'utilisateur par email
            UtilisateurResponseDto utilisateur = persistanceClient.getUtilisateurByEmail(email);

            // 2. Générer un token via ms-persistance
            String token = persistanceClient.generateResetToken(utilisateur.getId());

            // 3. Envoyer l'email
            emailService.sendPasswordResetEmail(email, token);

            log.info("✅ Email de réinitialisation envoyé à: {}", email);

        } catch (UtilisateurNotFoundException e) {
            // Ne rien faire pour ne pas révéler si l'email existe
            log.info("⚠️ Tentative de réinitialisation pour un email inexistant: {}", email);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("🔐 Réinitialisation du mot de passe avec token");

        // 1. Valider le token via ms-persistance
        TokenValidationDto validation = persistanceClient.validateToken(token);

        if (!validation.getValid()) {
            log.error("❌ Token invalide ou expiré");
            throw new BadCredentialsException("Token invalide, expiré ou déjà utilisé");
        }

        // 2. Hasher le nouveau mot de passe
        String hashedPassword = passwordEncoder.encode(newPassword);

        // 3. Mettre à jour le mot de passe via ms-persistance
        persistanceClient.updatePassword(validation.getUtilisateurId(), hashedPassword);

        // 4. Marquer le token comme utilisé
        persistanceClient.markTokenAsUsed(token);

        log.info("✅ Mot de passe réinitialisé avec succès pour l'utilisateur ID: {}",
                validation.getUtilisateurId());
    }
}