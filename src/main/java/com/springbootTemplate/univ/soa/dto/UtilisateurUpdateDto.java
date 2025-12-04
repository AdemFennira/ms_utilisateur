package com.springbootTemplate.univ.soa.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurUpdateDto {

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String nouveauMotDePasse;

    // met à jour les aliments exclus
    private Set<Long> alimentsExclusIds;
}
