package com.springbootTemplate.univ.soa.dto;

import jakarta.validation.constraints.Email;
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

    @Email(message = "L'email doit être valide")
    private String email;

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    // Ancien mot de passe (requis si on veut changer le mot de passe)
    private String ancienMotDePasse;

    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
    private String nouveauMotDePasse;

    private Set<Long> regimesIds;
    private Set<Long> allergenesIds;
    private Set<Long> typesCuisinePreferesIds;
}