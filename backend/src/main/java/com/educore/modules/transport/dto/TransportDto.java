package com.educore.modules.transport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalTime;

public class TransportDto {

    public record BusCreation(@NotBlank String immatriculation, @NotNull @Positive Integer capacite, Long chauffeurId) {}
    public record BusProfil(Long id, String immatriculation, Integer capacite, Long chauffeurId, String statut) {}

    public record ItineraireCreation(@NotNull Long busId, @NotBlank String libelle, LocalTime heureDepartMatin, LocalTime heureRetourSoir) {}
    public record ItineraireProfil(Long id, Long busId, String libelle, LocalTime heureDepartMatin, LocalTime heureRetourSoir) {}

    public record ArretCreation(@NotBlank String libelle, Double latitude, Double longitude, @NotNull Short ordre) {}
    public record ArretProfil(Long id, Long itineraireId, String libelle, Double latitude, Double longitude, Short ordre) {}

    public record AffectationEleve(@NotNull Long itineraireId, Long arretId) {}
    public record AffectationProfil(Long id, Long eleveId, Long itineraireId, Long arretId) {}
}
