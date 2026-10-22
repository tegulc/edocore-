package com.educore.modules.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleDto {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private Short dureeAnnees;
}
