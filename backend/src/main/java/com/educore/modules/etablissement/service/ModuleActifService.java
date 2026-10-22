package com.educore.modules.etablissement.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.etablissement.dto.ModuleActifDto;
import com.educore.modules.etablissement.entity.EtablissementModule;
import com.educore.modules.etablissement.repository.EtablissementModuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Parametrage des modules actifs par etablissement (cahier des charges 3.1 Administrateur). */
@Service
public class ModuleActifService {

    private final EtablissementModuleRepository etablissementModuleRepository;

    public ModuleActifService(EtablissementModuleRepository etablissementModuleRepository) {
        this.etablissementModuleRepository = etablissementModuleRepository;
    }

    public List<ModuleActifDto.Profil> lister(Long etablissementId) {
        Map<String, Boolean> actifs = etablissementModuleRepository.findByEtablissementId(etablissementId).stream()
                .collect(Collectors.toMap(EtablissementModule::getModuleCode, EtablissementModule::isActif));

        return ModuleCatalogue.MODULES_DISPONIBLES.stream()
                .map(code -> new ModuleActifDto.Profil(code, actifs.getOrDefault(code, false)))
                .sorted((a, b) -> a.moduleCode().compareTo(b.moduleCode()))
                .toList();
    }

    @Transactional
    public ModuleActifDto.Profil definir(Long etablissementId, String moduleCode, boolean actif) {
        if (!ModuleCatalogue.MODULES_DISPONIBLES.contains(moduleCode)) {
            throw new BusinessException("MODULE_INCONNU", "Module inconnu : " + moduleCode, HttpStatus.BAD_REQUEST);
        }
        EtablissementModule module = etablissementModuleRepository.findByEtablissementIdAndModuleCode(etablissementId, moduleCode)
                .orElseGet(() -> {
                    EtablissementModule nouveau = new EtablissementModule();
                    nouveau.setEtablissementId(etablissementId);
                    nouveau.setModuleCode(moduleCode);
                    return nouveau;
                });
        module.setActif(actif);
        module.setDateMaj(Instant.now());
        etablissementModuleRepository.save(module);
        return new ModuleActifDto.Profil(moduleCode, actif);
    }
}
