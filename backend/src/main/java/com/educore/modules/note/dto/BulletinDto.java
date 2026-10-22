package com.educore.modules.note.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public class BulletinDto {
    public record Profil(Long id, Long eleveId, String eleveNom, Long classeId, Long periodeId,
                          BigDecimal moyenneGenerale, Integer rang, String appreciationGenerale,
                          String decisionConseil, Integer nombreAbsences, String statut,
                          List<MoyenneDto.Matiere> moyennesParMatiere,
                          BigDecimal moyenneClasse, BigDecimal maxClasse, BigDecimal minClasse,
                          Integer effectif,
                          LocalDate dateConseil, String recompenses, Boolean avertissementTravail,
                          Boolean avertissementConduite, Boolean blame, Boolean exclusion,
                          String observationConseil, String decisionFinale, String classeOrientation,
                          Long valideParId, Instant dateValidation) {}

    public record Matiere(Long classeMatiereId, String matiereLibelle, BigDecimal moyenne, Integer rangMatiere,
                          String enseignantNom, BigDecimal coefficient, BigDecimal volumeHoraireHebdo,
                          BigDecimal maxClasse, BigDecimal minClasse, BigDecimal moyenneClasse,
                          String appreciation) {}

    public record ConseilClasse(LocalDate dateConseil, String recompenses, Boolean avertissementTravail,
                                Boolean avertissementConduite, Boolean blame, Boolean exclusion,
                                String observationConseil, String decisionFinale, String classeOrientation) {}
}
