package com.educore.modules.etablissement.dto;

public class ModuleActifDto {
    public record Profil(String moduleCode, boolean actif) {}
    public record Modification(boolean actif) {}
}
