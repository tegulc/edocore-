package com.educore.modules.note.service;

import com.educore.modules.note.entity.Bulletin;
import com.educore.modules.note.entity.TableauHonneur;
import com.educore.modules.note.repository.BulletinRepository;
import com.educore.modules.note.repository.TableauHonneurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service de gestion du tableau d'honneur.
 * Génère automatiquement les entrées lors de la publication des bulletins
 * si la moyenne générale de l'élève est >= 12/20.
 */
@Service
public class TableauHonneurService {

    private final TableauHonneurRepository tableauHonneurRepository;
    private final BulletinRepository bulletinRepository;

    public TableauHonneurService(TableauHonneurRepository tableauHonneurRepository,
                                  BulletinRepository bulletinRepository) {
        this.tableauHonneurRepository = tableauHonneurRepository;
        this.bulletinRepository = bulletinRepository;
    }

    @Transactional
    public void genererPourBulletin(Long bulletinId) {
        Bulletin bulletin = bulletinRepository.findById(bulletinId)
                .orElseThrow(() -> new RuntimeException("Bulletin non trouvé"));

        // Vérifier si l'élève est éligible (moyenne >= 12)
        if (bulletin.getMoyenneGenerale() != null && bulletin.getMoyenneGenerale().compareTo(BigDecimal.valueOf(12)) >= 0) {
            // Vérifier si une entrée existe déjà
            var existant = tableauHonneurRepository.findByEleveIdAndPeriodeId(
                    bulletin.getEleveId(), bulletin.getPeriodeId());

            if (existant.isEmpty()) {
                TableauHonneur tableauHonneur = new TableauHonneur();
                tableauHonneur.setEtablissementId(1L); // TODO: Récupérer depuis le bulletin
                tableauHonneur.setEleveId(bulletin.getEleveId());
                tableauHonneur.setClasseId(bulletin.getClasseId());
                tableauHonneur.setPeriodeId(bulletin.getPeriodeId());
                tableauHonneur.setMoyenneGenerale(bulletin.getMoyenneGenerale());
                tableauHonneur.setRang(bulletin.getRang());
                tableauHonneur.setMention(determinerMention(bulletin.getMoyenneGenerale()));
                tableauHonneur.setBulletinId(bulletin.getId());
                tableauHonneur.setGenereLe(Instant.now());
                tableauHonneur.setPublieLe(Instant.now());
                tableauHonneur.setStatut("actif");
                
                tableauHonneurRepository.save(tableauHonneur);
            }
        }
    }

    @Transactional
    public void genererPourClasse(Long etablissementId, Long classeId, Long periodeId) {
        List<Bulletin> bulletins = bulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId);
        
        for (Bulletin bulletin : bulletins) {
            if ("publie".equals(bulletin.getStatut())) {
                genererPourBulletin(bulletin.getId());
            }
        }
    }

    public List<TableauHonneur> listerPourClasse(Long etablissementId, Long classeId, Long periodeId) {
        return tableauHonneurRepository.findByClasseIdAndPeriodeIdOrderByMoyenneGeneraleDesc(classeId, periodeId);
    }

    public List<TableauHonneur> listerPourEtablissement(Long etablissementId, Long periodeId) {
        return tableauHonneurRepository.findByEtablissementIdAndPeriodeIdOrderByMoyenneGeneraleDesc(etablissementId, periodeId);
    }

    private String determinerMention(BigDecimal moyenne) {
        if (moyenne == null) return "—";
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) return "Excellent";
        if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) return "Très bien";
        if (moyenne.compareTo(BigDecimal.valueOf(12)) >= 0) return "Bien";
        return "Assez bien";
    }
}
