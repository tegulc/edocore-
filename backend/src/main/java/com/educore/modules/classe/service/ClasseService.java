package com.educore.modules.classe.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.dto.ClasseDto;
import com.educore.modules.classe.entity.Classe;
import com.educore.modules.classe.entity.Niveau;
import com.educore.modules.classe.entity.Serie;
import com.educore.modules.classe.repository.ClasseRepository;
import com.educore.modules.classe.repository.NiveauRepository;
import com.educore.modules.classe.repository.SerieRepository;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Classes de l'annee scolaire ACTIVE de l'etablissement (jamais une autre annee
 * implicitement - voir AnneeScolaireService, qui garantit qu'une seule annee
 * est active a la fois). Regle 01 : le niveau et la serie (si fournie) doivent
 * appartenir au meme etablissement, jamais devine ou emprunte a un autre tenant.
 */
@Service
public class ClasseService {

    private final ClasseRepository classeRepository;
    private final NiveauRepository niveauRepository;
    private final SerieRepository serieRepository;
    private final AnneeScolaireService anneeScolaireService;
    private final StructureSystemeEducatifService structureSystemeEducatifService;

    public ClasseService(ClasseRepository classeRepository, NiveauRepository niveauRepository,
                          SerieRepository serieRepository, AnneeScolaireService anneeScolaireService,
                          StructureSystemeEducatifService structureSystemeEducatifService) {
        this.classeRepository = classeRepository;
        this.niveauRepository = niveauRepository;
        this.serieRepository = serieRepository;
        this.anneeScolaireService = anneeScolaireService;
        this.structureSystemeEducatifService = structureSystemeEducatifService;
    }

    @Transactional
    public ClasseDto.Profil creer(Long etablissementId, ClasseDto.Creation requete) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();

        Niveau niveau = niveauRepository.findById(requete.niveauId())
                .filter(n -> n.getEtablissementId().equals(etablissementId))
                .orElseThrow(() -> new RessourceIntrouvableException("Niveau", requete.niveauId()));

        Serie serie = null;
        if (requete.serieId() != null) {
            serie = serieRepository.findById(requete.serieId())
                    .filter(s -> s.getEtablissementId().equals(etablissementId))
                    .orElseThrow(() -> new RessourceIntrouvableException("Serie", requete.serieId()));
        }

        if (classeRepository.existsByEtablissementIdAndAnneeScolaireIdAndLibelle(etablissementId, anneeActiveId, requete.libelle())) {
            throw new BusinessException("CLASSE_DEJA_EXISTANTE", "Une classe porte deja ce libelle pour l'annee active.", HttpStatus.CONFLICT);
        }

        Classe classe = new Classe();
        classe.setEtablissementId(etablissementId);
        classe.setAnneeScolaireId(anneeActiveId);
        classe.setNiveauId(niveau.getId());
        classe.setSerieId(serie != null ? serie.getId() : null);
        classe.setLibelle(requete.libelle());
        classe.setSalleId(requete.salleId());
        classe.setProfesseurPrincipalId(requete.professeurPrincipalId());
        if (requete.capacite() != null) classe.setCapacite(requete.capacite());
        classeRepository.save(classe);

        return versProfil(classe, niveau, serie);
    }

    @Transactional
    public ClasseDto.Profil modifier(Long etablissementId, Long classeId, ClasseDto.Modification requete) {
        Classe classe = trouver(etablissementId, classeId);
        Serie serie = null;
        if (requete.serieId() != null) {
            serie = serieRepository.findById(requete.serieId())
                    .filter(s -> s.getEtablissementId().equals(etablissementId))
                    .orElseThrow(() -> new RessourceIntrouvableException("Serie", requete.serieId()));
            classe.setSerieId(serie.getId());
        }
        if (requete.libelle() != null) classe.setLibelle(requete.libelle());
        if (requete.salleId() != null) classe.setSalleId(requete.salleId());
        if (requete.professeurPrincipalId() != null) classe.setProfesseurPrincipalId(requete.professeurPrincipalId());
        if (requete.capacite() != null) classe.setCapacite(requete.capacite());

        Niveau niveau = niveauRepository.findById(classe.getNiveauId()).orElseThrow();
        return versProfil(classe, niveau, serie);
    }

    public List<ClasseDto.Profil> lister(Long etablissementId) {
        Long anneeActiveId = anneeScolaireService.obtenirActive(etablissementId).id();
        return classeRepository.findByEtablissementIdAndAnneeScolaireIdOrderByLibelleAsc(etablissementId, anneeActiveId).stream()
                .map(c -> versProfil(c, niveauRepository.findById(c.getNiveauId()).orElse(null),
                        c.getSerieId() != null ? serieRepository.findById(c.getSerieId()).orElse(null) : null))
                .toList();
    }

    /** Point d'entree public pour les autres modules (ex: Eleves/Inscriptions) - jamais d'acces direct au repository d'un autre module. */
    public ClasseDto.Profil obtenir(Long etablissementId, Long classeId) {
        Classe classe = trouver(etablissementId, classeId);
        Niveau niveau = niveauRepository.findById(classe.getNiveauId()).orElse(null);
        Serie serie = classe.getSerieId() != null ? serieRepository.findById(classe.getSerieId()).orElse(null) : null;
        return versProfil(classe, niveau, serie);
    }

    Classe trouver(Long etablissementId, Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", classeId));
        if (!classe.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Classe", classeId);
        }
        return classe;
    }

    private ClasseDto.Profil versProfil(Classe c, Niveau niveau, Serie serie) {
        long effectif = classeRepository.compterElevesInscrits(c.getId());
        return new ClasseDto.Profil(c.getId(), c.getAnneeScolaireId(), c.getNiveauId(),
                niveau != null ? niveau.getCode() : null, c.getSerieId(), 
                serie != null ? serie.getCode() : null, serie != null ? serie.getSousSerieCode() : null,
                serie != null ? serie.getOrientation() : null,
                c.getLibelle(), c.getSalleId(), c.getProfesseurPrincipalId(), c.getCapacite(), effectif);
    }
}
