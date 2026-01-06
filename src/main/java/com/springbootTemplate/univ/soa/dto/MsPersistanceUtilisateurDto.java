package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO utilisé UNIQUEMENT pour communiquer avec MS-Persistance via PUT /api/persistance/utilisateurs/{id}
 * Ce DTO existe car :
 * 1. MS-Persistance exige un payload complet avec 'email' obligatoire (validation côté Persistance)
 * 2. UtilisateurUpdateDto (notre API publique) ne contient que les champs modifiables (nom, prenom, nouveauMotDePasse)
 * 3. UtilisateurResponseDto n'a pas de champ 'motDePasse' (bonne pratique : jamais renvoyer le mdp)
 * Solution : Ce DTO fusionne les données existantes + les modifications avant l'appel à MS-Persistance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MsPersistanceUtilisateurDto {

    private Long id;
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private Boolean actif;
    private Role role;
    private Set<Long> regimesIds;
    private Set<Long> allergenesIds;
    private Set<Long> typesCuisinePreferesIds;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}


