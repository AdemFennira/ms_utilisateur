package com.springbootTemplate.univ.soa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferencesDto {
    private String theme; // e.g., "dark", "light"
    private String language; // e.g., "fr", "en"
    private Boolean notificationsEnabled;
}