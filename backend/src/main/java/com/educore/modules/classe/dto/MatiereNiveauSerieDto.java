package com.educore.modules.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatiereNiveauSerieDto {
    private Long id;
    private Long etablissementId;
    private Long matiereId;
    private String matiereCode;
    private String matiereLibelle;
    private Long niveauId;
    private String niveauCode;
    private String niveauLibelle;
    private Long serieId;
    private String serieCode;
    private String serieLibelle;
    private BigDecimal coefficient;
    private BigDecimal volumeHoraireHebdo;
    private Boolean estObligatoire;
}
