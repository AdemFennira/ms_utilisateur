package com.springbootTemplate.univ.soa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationDto {
    private Boolean valid;
    private Long utilisateurId;
    private String message;
}