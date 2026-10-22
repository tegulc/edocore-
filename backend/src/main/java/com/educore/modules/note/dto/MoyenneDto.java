package com.educore.modules.note.dto;

import java.math.BigDecimal;

public class MoyenneDto {
    public record Matiere(Long classeMatiereId, String matiereLibelle, BigDecimal moyenne, Integer rangMatiere,
                          String enseignantNom, BigDecimal coefficient, BigDecimal volumeHoraireHebdo,
                          BigDecimal maxClasse, BigDecimal minClasse, BigDecimal moyenneClasse,
                          String appreciation) {}
    public record Generale(Long eleveId, String eleveNom, BigDecimal moyenneGenerale, Integer rangClasse, String decision) {}
}
