package com.springbootTemplate.univ.soa.client;

import com.springbootTemplate.univ.soa.dto.*;
import com.springbootTemplate.univ.soa.exception.UtilisateurNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistanceClient {

    private final RestTemplate restTemplate;

    @Value("${persistance.service.url}")
    private String persistanceServiceUrl;

    private static final String UTILISATEURS_PATH = "/api/persistance/utilisateurs";

    /**
     * Créer un utilisateur via ms-persistance
     */
    public UtilisateurResponseDto createUtilisateur(UtilisateurCreateDto createDto) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH;

        try {
            log.info("📤 Appel POST vers ms-persistance: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UtilisateurCreateDto> request = new HttpEntity<>(createDto, headers);

            ResponseEntity<UtilisateurResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    UtilisateurResponseDto.class
            );

            log.info("✅ Utilisateur créé avec succès via ms-persistance");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public UtilisateurResponseDto getUtilisateurById(Long id) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/" + id;

        try {
            log.info("📤 Appel GET vers ms-persistance: {}", url);

            ResponseEntity<UtilisateurResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    UtilisateurResponseDto.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Utilisateur non trouvé avec l'ID: {}", id);
            throw new UtilisateurNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
    }

    /**
     * Récupérer un utilisateur par email
     */
    public UtilisateurResponseDto getUtilisateurByEmail(String email) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/email/" + email;

        try {
            log.info("📤 Appel GET vers ms-persistance pour l'email: {}", email);

            ResponseEntity<UtilisateurResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    UtilisateurResponseDto.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Utilisateur non trouvé avec l'email: {}", email);
            throw new UtilisateurNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<UtilisateurResponseDto> getAllUtilisateurs() {
        String url = persistanceServiceUrl + UTILISATEURS_PATH;

        try {
            log.info("📤 Appel GET vers ms-persistance pour récupérer tous les utilisateurs");

            ResponseEntity<List<UtilisateurResponseDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    public UtilisateurResponseDto updateUtilisateur(Long id, MsPersistanceUtilisateurDto fullDto) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/" + id;

        try {
            log.info("📤 Appel PUT vers ms-persistance: {}", url);
            log.debug("DTO envoyé: email={}, nom={}, prenom={}, motDePasse={}",
                    fullDto.getEmail(), fullDto.getNom(), fullDto.getPrenom(),
                    fullDto.getMotDePasse() != null ? "[MODIFIÉ]" : "[NON MODIFIÉ]");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MsPersistanceUtilisateurDto> request = new HttpEntity<>(fullDto, headers);

            ResponseEntity<UtilisateurResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    UtilisateurResponseDto.class
            );

            log.info("✅ Utilisateur mis à jour avec succès via ms-persistance");
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Utilisateur non trouvé avec l'ID: {}", id);
            throw new UtilisateurNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur HTTP {} lors de la mise à jour: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de la mise à jour: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public void deleteUtilisateur(Long id) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/" + id;

        try {
            log.info("📤 Appel DELETE vers ms-persistance: {}", url);

            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            log.info("✅ Utilisateur supprimé avec succès via ms-persistance");

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Utilisateur non trouvé avec l'ID: {}", id);
            throw new UtilisateurNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
    }

    /**
     * Vérifier si un email existe déjà
     */
    public boolean existsByEmail(String email) {
        try {
            getUtilisateurByEmail(email);
            return true;
        } catch (UtilisateurNotFoundException e) {
            return false;
        }
    }

    /**
     * Récupérer un utilisateur avec son hash de mot de passe pour l'authentification
     */
    public UtilisateurAuthDto getUtilisateurForAuth(String email) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/auth/" + email;

        try {
            log.info("📤 Appel GET vers ms-persistance pour authentification: {}", email);

            ResponseEntity<UtilisateurAuthDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    UtilisateurAuthDto.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Utilisateur non trouvé avec l'email: {}", email);
            throw new UtilisateurNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }
    }

    /**
     * Générer un token de réinitialisation
     */
    public String generateResetToken(Long utilisateurId) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/" + utilisateurId + "/generate-reset-token";

        try {
            log.info("📤 Génération du token de réinitialisation pour l'utilisateur ID: {}", utilisateurId);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Map.class
            );

            String token = (String) response.getBody().get("token");
            log.info("✅ Token généré avec succès");
            return token;

        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur lors de la génération du token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Valider un token de réinitialisation
     */
    public TokenValidationDto validateToken(String token) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/validate-token/" + token;

        try {
            log.info("📤 Validation du token de réinitialisation");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );

            Map<String, Object> body = response.getBody();

            return TokenValidationDto.builder()
                    .valid((Boolean) body.get("valid"))
                    .utilisateurId(body.get("utilisateurId") != null ?
                            ((Number) body.get("utilisateurId")).longValue() : null)
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("❌ Token invalide ou expiré");
            return TokenValidationDto.builder()
                    .valid(false)
                    .message("Token invalide ou expiré")
                    .build();
        }
    }

    /**
     * Mettre à jour le mot de passe
     */
    public void updatePassword(Long utilisateurId, String hashedPassword) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/" + utilisateurId + "/update-password";

        try {
            log.info("📤 Mise à jour du mot de passe pour l'utilisateur ID: {}", utilisateurId);

            Map<String, String> request = Map.of("hashedPassword", hashedPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );

            log.info("✅ Mot de passe mis à jour avec succès");

        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur lors de la mise à jour du mot de passe: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Marquer un token comme utilisé
     */
    public void markTokenAsUsed(String token) {
        String url = persistanceServiceUrl + UTILISATEURS_PATH + "/mark-token-used/" + token;

        try {
            log.info("📤 Marquage du token comme utilisé");

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Map.class
            );

            log.info("✅ Token marqué comme utilisé");

        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur lors du marquage du token: {}", e.getMessage());
        }
    }
}