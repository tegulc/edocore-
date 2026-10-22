package com.educore.modules.classe.service;

import com.educore.modules.classe.dto.*;
import com.educore.modules.classe.entity.*;
import com.educore.modules.classe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StructureSystemeEducatifService {

    private final SousSystemeRepository sousSystemeRepository;
    private final CycleRepository cycleRepository;
    private final NiveauRepository niveauRepository;
    private final SerieRepository serieRepository;
    private final MatiereRepository matiereRepository;
    private final MatiereNiveauSerieRepository matiereNiveauSerieRepository;
    private final EtablissementLangueOptionRepository etablissementLangueOptionRepository;

    // Récupérer les référentiels globaux
    public List<SousSystemeDto> listerSousSystemes() {
        return sousSystemeRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<CycleDto> listerCycles() {
        return cycleRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<SerieDto.Profil> listerSeriesParSousSysteme(String sousSystemeCode) {
        return serieRepository.findAll().stream()
                .filter(s -> {
                    SousSysteme ss = sousSystemeRepository.findById(s.getSousSystemeId()).orElse(null);
                    return ss != null && ss.getCode().equals(sousSystemeCode);
                })
                .map(this::mapToDto)
                .toList();
    }

    public List<MatiereDto.Profil> listerMatieresParType(String typeMatiere) {
        return matiereRepository.findAll().stream()
                .filter(m -> typeMatiere == null || m.getTypeMatiere() == null || m.getTypeMatiere().equals(typeMatiere))
                .map(this::mapToDto)
                .toList();
    }

    // Initialiser les niveaux par défaut pour un établissement camerounais
    @Transactional
    public void initialiserNiveauxParDefaut(Long etablissementId, String sousSystemeCode) {
        Cycle premierCycle = cycleRepository.findByCode("PREMIER_CYCLE")
                .orElseThrow(() -> new IllegalArgumentException("Cycle PREMIER_CYCLE non trouvé"));
        Cycle secondCycle = cycleRepository.findByCode("SECOND_CYCLE")
                .orElseThrow(() -> new IllegalArgumentException("Cycle SECOND_CYCLE non trouvé"));

        // Niveaux du 1er cycle (6e, 5e, 4e, 3e)
        creerNiveauSiNExistePas(etablissementId, "6e", "Sixième", (short) 1, "PREMIER_CYCLE");
        creerNiveauSiNExistePas(etablissementId, "5e", "Cinquième", (short) 2, "PREMIER_CYCLE");
        creerNiveauSiNExistePas(etablissementId, "4e", "Quatrième", (short) 3, "PREMIER_CYCLE");
        creerNiveauSiNExistePas(etablissementId, "3e", "Troisième", (short) 4, "PREMIER_CYCLE");

        // Niveaux du 2nd cycle (2nde, 1ère, Tle)
        creerNiveauSiNExistePas(etablissementId, "2nde", "Seconde", (short) 5, "SECOND_CYCLE");
        creerNiveauSiNExistePas(etablissementId, "1ere", "Première", (short) 6, "SECOND_CYCLE");
        creerNiveauSiNExistePas(etablissementId, "Tle", "Terminale", (short) 7, "SECOND_CYCLE");
    }

    private void creerNiveauSiNExistePas(Long etablissementId, String code, String libelle, Short ordre, String cycleCode) {
        if (niveauRepository.findByEtablissementIdAndCode(etablissementId, code).isEmpty()) {
            Niveau niveau = new Niveau();
            niveau.setEtablissementId(etablissementId);
            niveau.setCode(code);
            niveau.setLibelle(libelle);
            niveau.setOrdre(ordre);
            niveau.setCycle(cycleCode);
            niveauRepository.save(niveau);
        }
    }

    // Configuration des matières par niveau et série
    @Transactional
    public MatiereNiveauSerieDto configurerMatiereNiveauSerie(MatiereNiveauSerieDto dto) {
        MatiereNiveauSerie entity = new MatiereNiveauSerie();
        entity.setEtablissementId(dto.getEtablissementId());
        entity.setMatiereId(dto.getMatiereId());
        entity.setNiveauId(dto.getNiveauId());
        entity.setSerieId(dto.getSerieId());
        entity.setCoefficient(dto.getCoefficient() != null ? dto.getCoefficient() : BigDecimal.ONE);
        entity.setVolumeHoraireHebdo(dto.getVolumeHoraireHebdo() != null ? dto.getVolumeHoraireHebdo() : BigDecimal.ONE);
        entity.setEstObligatoire(dto.getEstObligatoire() != null ? dto.getEstObligatoire() : true);

        MatiereNiveauSerie saved = matiereNiveauSerieRepository.save(entity);
        return mapToDto(saved);
    }

    public List<MatiereNiveauSerieDto> listerMatieresParNiveau(Long etablissementId, Long niveauId) {
        return matiereNiveauSerieRepository.findByEtablissementIdAndNiveauId(etablissementId, niveauId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<MatiereNiveauSerieDto> listerMatieresParNiveauEtSerie(Long etablissementId, Long niveauId, Long serieId) {
        return matiereNiveauSerieRepository.findByEtablissementIdAndNiveauIdAndSerieId(etablissementId, niveauId, serieId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Configuration des langues/options par établissement
    @Transactional
    public EtablissementLangueOptionDto ajouterLangueOption(EtablissementLangueOptionDto dto) {
        EtablissementLangueOption entity = new EtablissementLangueOption();
        entity.setEtablissementId(dto.getEtablissementId());
        entity.setMatiereId(dto.getMatiereId());
        entity.setTypeOffre(dto.getTypeOffre());
        entity.setNiveauMinId(dto.getNiveauMinId());

        EtablissementLangueOption saved = etablissementLangueOptionRepository.save(entity);
        return mapToDto(saved);
    }

    public List<EtablissementLangueOptionDto> listerLanguesOptions(Long etablissementId) {
        return etablissementLangueOptionRepository.findByEtablissementId(etablissementId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Mapping methods
    private SousSystemeDto mapToDto(SousSysteme entity) {
        return SousSystemeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                .build();
    }

    private CycleDto mapToDto(Cycle entity) {
        return CycleDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .libelle(entity.getLibelle())
                .description(entity.getDescription())
                .dureeAnnees(entity.getDureeAnnees())
                .build();
    }

    private SerieDto.Profil mapToDto(Serie entity) {
        return new SerieDto.Profil(
                entity.getId(),
                entity.getCode(),
                entity.getSousSerieCode(),
                entity.getLibelle(),
                entity.getSerieParentId(),
                entity.getSousSystemeId(),
                entity.getOrientation(),
                entity.getDescription()
        );
    }

    private MatiereDto.Profil mapToDto(Matiere entity) {
        return new MatiereDto.Profil(
                entity.getId(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getCouleur(),
                entity.getTypeMatiere(),
                entity.getLangueVivanteNiveau(),
                entity.getIsOption(),
                entity.getDescription()
        );
    }

    private MatiereNiveauSerieDto mapToDto(MatiereNiveauSerie entity) {
        MatiereNiveauSerieDto dto = MatiereNiveauSerieDto.builder()
                .id(entity.getId())
                .etablissementId(entity.getEtablissementId())
                .matiereId(entity.getMatiereId())
                .niveauId(entity.getNiveauId())
                .serieId(entity.getSerieId())
                .coefficient(entity.getCoefficient())
                .volumeHoraireHebdo(entity.getVolumeHoraireHebdo())
                .estObligatoire(entity.getEstObligatoire())
                .build();

        // Charger les libellés associés
        matiereRepository.findById(entity.getMatiereId()).ifPresent(m -> {
            dto.setMatiereCode(m.getCode());
            dto.setMatiereLibelle(m.getLibelle());
        });

        niveauRepository.findById(entity.getNiveauId()).ifPresent(n -> {
            dto.setNiveauCode(n.getCode());
            dto.setNiveauLibelle(n.getLibelle());
        });

        if (entity.getSerieId() != null) {
            serieRepository.findById(entity.getSerieId()).ifPresent(s -> {
                dto.setSerieCode(s.getCode());
                dto.setSerieLibelle(s.getLibelle());
            });
        }

        return dto;
    }

    private EtablissementLangueOptionDto mapToDto(EtablissementLangueOption entity) {
        EtablissementLangueOptionDto dto = EtablissementLangueOptionDto.builder()
                .id(entity.getId())
                .etablissementId(entity.getEtablissementId())
                .matiereId(entity.getMatiereId())
                .typeOffre(entity.getTypeOffre())
                .niveauMinId(entity.getNiveauMinId())
                .build();

        matiereRepository.findById(entity.getMatiereId()).ifPresent(m -> {
            dto.setMatiereCode(m.getCode());
            dto.setMatiereLibelle(m.getLibelle());
        });

        if (entity.getNiveauMinId() != null) {
            niveauRepository.findById(entity.getNiveauMinId()).ifPresent(n -> {
                dto.setNiveauMinCode(n.getCode());
            });
        }

        return dto;
    }
}
