package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurAuthDto {
    private Long id;
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private Boolean actif;
    private Role role;
}