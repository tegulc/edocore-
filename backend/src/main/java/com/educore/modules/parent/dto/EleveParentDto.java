package com.educore.modules.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EleveParentDto {

    public record Liaison(@NotNull Long parentId, @NotBlank String lienParente,
                           boolean estContactPrincipal, boolean autoriseRecupererEleve) {}

    public record Profil(Long id, Long eleveId, Long parentId, String parentNom, String parentPrenom,
                          String parentTelephone, String lienParente, boolean estContactPrincipal, boolean autoriseRecupererEleve) {}
}
