package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Créer un nouveau compte utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<UtilisateurResponseDto> register(@Valid @RequestBody UtilisateurCreateDto createDto) {
        log.info("POST /api/utilisateurs/register - Inscription");
        UtilisateurResponseDto response = utilisateurService.register(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur", description = "Authentifier un utilisateur et obtenir un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    })
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto loginDto) {
        log.info("POST /api/utilisateurs/login - Connexion: {}", loginDto.getEmail());
        String token = utilisateurService.login(loginDto);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer un utilisateur par ID", description = "Obtenir les détails d'un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<UtilisateurResponseDto> getUtilisateurById(@PathVariable Long id) {
        log.info("GET /api/utilisateurs/{} - Récupération", id);
        UtilisateurResponseDto response = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer un utilisateur par email")
    public ResponseEntity<UtilisateurResponseDto> getUtilisateurByEmail(@PathVariable String email) {
        log.info("GET /api/utilisateurs/email/{} - Récupération", email);
        UtilisateurResponseDto response = utilisateurService.getUtilisateurByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer tous les utilisateurs (Admin seulement)")
    public ResponseEntity<List<UtilisateurResponseDto>> getAllUtilisateurs() {
        log.info("GET /api/utilisateurs - Récupération de tous les utilisateurs");
        List<UtilisateurResponseDto> response = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<UtilisateurResponseDto> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDto updateDto) {
        log.info("PUT /api/utilisateurs/{} - Mise à jour", id);
        UtilisateurResponseDto response = utilisateurService.updateUtilisateur(id, updateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer un utilisateur (Admin seulement)")
    public ResponseEntity<Map<String, String>> deleteUtilisateur(@PathVariable Long id) {
        log.info("DELETE /api/utilisateurs/{} - Suppression", id);
        utilisateurService.deleteUtilisateur(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur supprimé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demander la réinitialisation du mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé"),
            @ApiResponse(responseCode = "400", description = "Email invalide")
    })
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto) {
        log.info("POST /api/utilisateurs/forgot-password - Email: {}", dto.getEmail());

        utilisateurService.forgotPassword(dto.getEmail());

        return ResponseEntity.ok(Map.of(
                "message",
                "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé"
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe avec un token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
            @ApiResponse(responseCode = "401", description = "Token invalide ou expiré")
    })
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        log.info("POST /api/utilisateurs/reset-password - Token reçu");

        try {
            utilisateurService.resetPassword(dto.getToken(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } catch (BadCredentialsException e) {
            log.error("❌ Erreur lors de la réinitialisation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide, expiré ou déjà utilisé"));
        }
    }
}