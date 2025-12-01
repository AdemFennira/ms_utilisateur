package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurResponseDto {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private Boolean actif;
    private Set<Long> alimentsExclusIds;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
