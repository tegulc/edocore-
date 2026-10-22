package com.educore.modules.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementLangueOptionDto {
    private Long id;
    private Long etablissementId;
    private Long matiereId;
    private String matiereCode;
    private String matiereLibelle;
    private String typeOffre;
    private Long niveauMinId;
    private String niveauMinCode;
}
