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
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // =========================
    // AUTH (EXISTANT)
    // =========================

    @PostMapping("/register")
    public ResponseEntity<UtilisateurResponseDto> register(
            @Valid @RequestBody UtilisateurCreateDto createDto) {
        UtilisateurResponseDto response = utilisateurService.register(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginDto loginDto) {

        String token = utilisateurService.login(loginDto);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");

        return ResponseEntity.ok(response);
    }

    // =========================
    // 👤 UTILISATEUR CONNECTÉ (/me)
    // =========================

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil de l'utilisateur connecté")
    public ResponseEntity<UtilisateurResponseDto> getMe(Authentication authentication) {
        return ResponseEntity.ok(
                utilisateurService.getUtilisateurConnecte(authentication)
        );
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour le profil de l'utilisateur connecté")
    public ResponseEntity<UtilisateurResponseDto> updateMe(
            Authentication authentication,
            @Valid @RequestBody UtilisateurUpdateDto updateDto) {

        return ResponseEntity.ok(
                utilisateurService.updateUtilisateurConnecte(authentication, updateDto)
        );
    }

    @DeleteMapping("/me")
    @Operation(summary = "Supprimer son propre compte")
    public ResponseEntity<Map<String, String>> deleteMe(Authentication authentication) {

        utilisateurService.deleteUtilisateurConnecte(authentication);
        return ResponseEntity.ok(Map.of(
                "message", "Compte utilisateur supprimé avec succès"
        ));
    }

    // =========================
    // ⚙️ PRÉFÉRENCES
    // =========================

    @GetMapping("/me/preferences")
    @Operation(summary = "Récupérer les préférences utilisateur")
    public ResponseEntity<PreferencesDto> getPreferences(Authentication authentication) {

        return ResponseEntity.ok(
                utilisateurService.getPreferences(authentication)
        );
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Mettre à jour les préférences utilisateur")
    public ResponseEntity<PreferencesDto> updatePreferences(
            Authentication authentication,
            @Valid @RequestBody PreferencesDto preferencesDto) {

        return ResponseEntity.ok(
                utilisateurService.updatePreferences(authentication, preferencesDto)
        );
    }

    // =========================
    // 📦 EXPORT DONNÉES (RGPD)
    // =========================

    @GetMapping("/me/export")
    @Operation(summary = "Exporter toutes les données de l'utilisateur (RGPD)")
    public ResponseEntity<byte[]> exportUserData(Authentication authentication) {

        byte[] zip = utilisateurService.exportUserData(authentication);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=donnees-utilisateur.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

    // =========================
    // 🛡️ ROUTES ADMIN (EXISTANTES)
    // =========================

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UtilisateurResponseDto> getUtilisateurById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer tous les utilisateurs (Admin seulement)")
    public ResponseEntity<List<UtilisateurResponseDto>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<UtilisateurResponseDto> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDto updateDto) {
        return ResponseEntity.ok(
                utilisateurService.updateUtilisateur(id, updateDto)
        );
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Map<String, String>> deleteUtilisateur(@PathVariable Long id) {
        log.info("DELETE /api/utilisateurs/{} - Suppression", id);
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé"));
    }

    // =========================
    // ❤️ HEALTH
    // =========================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
