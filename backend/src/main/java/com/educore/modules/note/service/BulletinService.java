package com.educore.modules.note.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.dto.ClasseMatiereDto;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.eleve.service.InscriptionService;
import com.educore.modules.etablissement.service.PeriodeService;
import com.educore.modules.note.dto.BulletinDto;
import com.educore.modules.note.dto.MoyenneDto;
import com.educore.modules.note.entity.Bulletin;
import com.educore.modules.note.entity.MoyenneGenerale;
import com.educore.modules.note.entity.MoyenneMatiere;
import com.educore.modules.note.entity.TableauHonneur;
import com.educore.modules.note.repository.BulletinRepository;
import com.educore.modules.note.repository.MoyenneGeneraleRepository;
import com.educore.modules.note.repository.MoyenneMatiereRepository;
import com.educore.modules.note.repository.TableauHonneurRepository;
import com.educore.common.integration.NotificationClient;
import com.educore.modules.parent.service.EleveParentService;
import com.educore.modules.parent.service.ParentService;
import com.educore.modules.presence.service.PresenceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bulletins (Module 8). Genere a partir des moyennes deja calculees
 * (MoyenneCalculService.recalculerPourClasse doit avoir tourne avant).
 * Workflow : brouillon -> valide (CENSEUR/DIRECTEUR, verrouille) -> publie
 * (visible parents/eleves - declenche une notification, Module 24 a venir).
 */
@Service
public class BulletinService {

    private final BulletinRepository bulletinRepository;
    private final MoyenneGeneraleRepository moyenneGeneraleRepository;
    private final MoyenneMatiereRepository moyenneMatiereRepository;
    private final InscriptionService inscriptionService;
    private final EleveService eleveService;
    private final PeriodeService periodeService;
    private final PresenceService presenceService;
    private final ClasseMatiereService classeMatiereService;
    private final EleveParentService eleveParentService;
    private final ParentService parentService;
    private final NotificationClient notificationClient;
    private final MoyenneCalculService moyenneCalculService;
    private final TableauHonneurService tableauHonneurService;

    public BulletinService(BulletinRepository bulletinRepository, MoyenneGeneraleRepository moyenneGeneraleRepository,
                            MoyenneMatiereRepository moyenneMatiereRepository, InscriptionService inscriptionService,
                            EleveService eleveService, PeriodeService periodeService, PresenceService presenceService,
                            ClasseMatiereService classeMatiereService, EleveParentService eleveParentService,
                            ParentService parentService, NotificationClient notificationClient,
                            MoyenneCalculService moyenneCalculService, TableauHonneurService tableauHonneurService) {
        this.bulletinRepository = bulletinRepository;
        this.moyenneGeneraleRepository = moyenneGeneraleRepository;
        this.moyenneMatiereRepository = moyenneMatiereRepository;
        this.inscriptionService = inscriptionService;
        this.eleveService = eleveService;
        this.periodeService = periodeService;
        this.presenceService = presenceService;
        this.classeMatiereService = classeMatiereService;
        this.eleveParentService = eleveParentService;
        this.parentService = parentService;
        this.notificationClient = notificationClient;
        this.moyenneCalculService = moyenneCalculService;
        this.tableauHonneurService = tableauHonneurService;
    }

    @Transactional
    public List<BulletinDto.Profil> genererPourClasse(Long etablissementId, Long classeId, Long periodeId) {
        var periode = periodeService.obtenir(etablissementId, periodeId);
        List<Long> eleveIds = inscriptionService.listerElevesInscrits(classeId);
        Map<Long, MoyenneCalculService.StatistiquesMatiere> statistiques = 
                moyenneCalculService.calculerStatistiquesClasse(etablissementId, classeId, periodeId);

        return eleveIds.stream().map(eleveId -> {
            MoyenneGenerale moyenneGenerale = moyenneGeneraleRepository.findByEleveIdAndPeriodeId(eleveId, periodeId)
                    .orElseThrow(() -> new BusinessException("MOYENNES_NON_CALCULEES",
                            "Les moyennes n'ont pas encore ete calculees pour cette periode. Lancez le recalcul d'abord.", HttpStatus.CONFLICT));

            var bilanPresence = presenceService.bilan(etablissementId, eleveId, periode.dateDebut(), periode.dateFin());

            Bulletin bulletin = bulletinRepository.findByEleveIdAndPeriodeId(eleveId, periodeId).orElseGet(Bulletin::new);
            bulletin.setEleveId(eleveId);
            bulletin.setClasseId(classeId);
            bulletin.setPeriodeId(periodeId);
            bulletin.setMoyenneGenerale(moyenneGenerale.getMoyenneGenerale());
            bulletin.setRang(moyenneGenerale.getRangClasse());
            bulletin.setDecisionConseil(moyenneGenerale.getDecision());
            bulletin.setNombreAbsences((int) bilanPresence.absencesNonJustifiees());
            bulletin.setStatut("brouillon");
            bulletin.setGenereLe(Instant.now());
            bulletinRepository.save(bulletin);

            return versProfil(etablissementId, bulletin, eleveId, statistiques);
        }).toList();
    }

    @Transactional
    public BulletinDto.Profil valider(Long etablissementId, Long bulletinId) {
        Bulletin bulletin = trouver(bulletinId);
        eleveService.obtenir(etablissementId, bulletin.getEleveId()); // verifie le tenant
        if (!"brouillon".equals(bulletin.getStatut())) {
            throw new BusinessException("BULLETIN_DEJA_TRAITE", "Ce bulletin n'est plus au statut brouillon.", HttpStatus.CONFLICT);
        }
        bulletin.setStatut("valide");
        return versProfil(etablissementId, bulletin, bulletin.getEleveId(), null);
    }

    @Transactional
    public BulletinDto.Profil publier(Long etablissementId, Long bulletinId) {
        Bulletin bulletin = trouver(bulletinId);
        eleveService.obtenir(etablissementId, bulletin.getEleveId());
        if (!"valide".equals(bulletin.getStatut())) {
            throw new BusinessException("BULLETIN_NON_VALIDE", "Le bulletin doit d'abord etre valide avant publication.", HttpStatus.CONFLICT);
        }
        bulletin.setStatut("publie");

        String titre = "Bulletin disponible";
        String eleveNom = eleveService.obtenir(etablissementId, bulletin.getEleveId()).prenom();
        String contenu = "Le bulletin de %s est maintenant disponible (moyenne : %s/20).".formatted(eleveNom, bulletin.getMoyenneGenerale());
        eleveParentService.listerParents(etablissementId, bulletin.getEleveId()).forEach(lien -> {
            Long utilisateurId = parentService.obtenirUtilisateurId(etablissementId, lien.parentId());
            notificationClient.envoyer(etablissementId, utilisateurId, "notes", titre, contenu, "sms");
        });

        // Générer automatiquement l'entrée dans le tableau d'honneur si éligible
        tableauHonneurService.genererPourBulletin(bulletin.getId());

        return versProfil(etablissementId, bulletin, bulletin.getEleveId(), null);
    }

    public BulletinDto.Profil obtenirPourEleve(Long etablissementId, Long eleveId, Long periodeId) {
        eleveService.obtenir(etablissementId, eleveId);
        Bulletin bulletin = bulletinRepository.findByEleveIdAndPeriodeId(eleveId, periodeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Bulletin", eleveId));
        Map<Long, MoyenneCalculService.StatistiquesMatiere> statistiques = 
                moyenneCalculService.calculerStatistiquesClasse(etablissementId, bulletin.getClasseId(), periodeId);
        return versProfil(etablissementId, bulletin, eleveId, statistiques);
    }

    private Bulletin trouver(Long bulletinId) {
        return bulletinRepository.findById(bulletinId)
                .orElseThrow(() -> new RessourceIntrouvableException("Bulletin", bulletinId));
    }

    private BulletinDto.Profil versProfil(Long etablissementId, Bulletin b, Long eleveId, 
                                          Map<Long, MoyenneCalculService.StatistiquesMatiere> statistiques) {
        var eleve = eleveService.obtenir(etablissementId, eleveId);
        List<ClasseMatiereDto.Profil> matieres = classeMatiereService.lister(etablissementId, b.getClasseId());
        Map<Long, ClasseMatiereDto.Profil> matieresParId = matieres.stream().collect(Collectors.toMap(ClasseMatiereDto.Profil::id, m -> m));

        List<MoyenneDto.Matiere> moyennesParMatiere = moyenneMatiereRepository.findByEleveIdAndPeriodeId(eleveId, b.getPeriodeId()).stream()
                .map(mm -> {
                    ClasseMatiereDto.Profil matiere = matieresParId.get(mm.getClasseMatiereId());
                    MoyenneCalculService.StatistiquesMatiere stats = statistiques != null ? 
                            statistiques.get(mm.getClasseMatiereId()) : null;
                    
                    String enseignantNom = "À définir";
                    BigDecimal coefficient = matiere != null ? matiere.coefficient() : BigDecimal.ONE;
                    BigDecimal volumeHoraire = matiere != null ? matiere.volumeHoraireHebdo() : BigDecimal.ONE;
                    BigDecimal maxClasse = stats != null ? stats.max() : null;
                    BigDecimal minClasse = stats != null ? stats.min() : null;
                    BigDecimal moyenneClasse = stats != null ? stats.moyenneClasse() : null;
                    String appreciation = moyenneCalculService.obtenirAppreciationMatiere(mm.getMoyenne());
                    
                    return new MoyenneDto.Matiere(mm.getClasseMatiereId(),
                            matiere != null ? matiere.matiereLibelle() : "?",
                            mm.getMoyenne(), mm.getRangMatiere(),
                            enseignantNom, coefficient, volumeHoraire,
                            maxClasse, minClasse, moyenneClasse,
                            appreciation);
                })
                .toList();

        // Calculer les statistiques de classe pour la moyenne générale
        List<MoyenneGenerale> moyennesGeneralesClasse = moyenneGeneraleRepository.findByClasseIdAndPeriodeId(b.getClasseId(), b.getPeriodeId());
        BigDecimal maxClasse = moyennesGeneralesClasse.stream()
                .map(MoyenneGenerale::getMoyenneGenerale)
                .filter(m -> m != null)
                .max(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal minClasse = moyennesGeneralesClasse.stream()
                .map(MoyenneGenerale::getMoyenneGenerale)
                .filter(m -> m != null)
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal moyenneClasse = moyennesGeneralesClasse.stream()
                .map(MoyenneGenerale::getMoyenneGenerale)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(moyennesGeneralesClasse.size()), java.math.MathContext.DECIMAL32);
        
        int effectif = inscriptionService.listerElevesInscrits(b.getClasseId()).size();

        return new BulletinDto.Profil(b.getId(), eleveId, eleve.prenom() + " " + eleve.nom(), b.getClasseId(), b.getPeriodeId(),
                b.getMoyenneGenerale(), b.getRang(), b.getAppreciationGenerale(), b.getDecisionConseil(),
                b.getNombreAbsences(), b.getStatut(), moyennesParMatiere,
                moyenneClasse, maxClasse, minClasse, effectif,
                b.getDateConseil(), b.getRecompenses(), b.getAvertissementTravail(),
                b.getAvertissementConduite(), b.getBlame(), b.getExclusion(),
                b.getObservationConseil(), b.getDecisionFinale(), b.getClasseOrientation(),
                b.getValideParId(), b.getDateValidation());
    }

    @Transactional
    public BulletinDto.Profil enregistrerConseilClasse(Long etablissementId, Long bulletinId, BulletinDto.ConseilClasse conseil) {
        Bulletin bulletin = trouver(bulletinId);
        eleveService.obtenir(etablissementId, bulletin.getEleveId()); // verifie le tenant
        
        if (!"valide".equals(bulletin.getStatut())) {
            throw new BusinessException("BULLETIN_NON_VALIDE", "Le bulletin doit être validé avant d'enregistrer le conseil de classe.", HttpStatus.CONFLICT);
        }
        
        bulletin.setDateConseil(conseil.dateConseil());
        bulletin.setRecompenses(conseil.recompenses());
        bulletin.setAvertissementTravail(conseil.avertissementTravail());
        bulletin.setAvertissementConduite(conseil.avertissementConduite());
        bulletin.setBlame(conseil.blame());
        bulletin.setExclusion(conseil.exclusion());
        bulletin.setObservationConseil(conseil.observationConseil());
        bulletin.setDecisionFinale(conseil.decisionFinale());
        bulletin.setClasseOrientation(conseil.classeOrientation());
        
        bulletinRepository.save(bulletin);
        
        return versProfil(etablissementId, bulletin, bulletin.getEleveId(), null);
    }
}
