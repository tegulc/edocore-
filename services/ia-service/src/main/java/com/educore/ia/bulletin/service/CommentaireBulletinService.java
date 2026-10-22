package com.educore.ia.bulletin.service;

import com.educore.ia.bulletin.dto.CommentaireBulletinDto;
import com.educore.ia.bulletin.entity.BulletinProjection;
import com.educore.ia.bulletin.entity.CommentaireBulletin;
import com.educore.ia.bulletin.repository.BulletinProjectionRepository;
import com.educore.ia.bulletin.repository.CommentaireBulletinRepository;
import com.educore.ia.common.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Generation de commentaires de bulletin (Module 25). PAR TEMPLATES
 * DETERMINISTES, pas un appel a un modele de langage reel (aucun acces
 * reseau/cle API LLM dans cet environnement de developpement) - a remplacer
 * par un vrai appel LLM en production (voir TODO). Le principe reste
 * identique quelle que soit l'implementation : jamais publie tel quel,
 * TOUJOURS soumis a validation/edition par l'enseignant avant d'apparaitre
 * sur le bulletin final (voir document d'architecture, section IA).
 */
@Service
public class CommentaireBulletinService {

    private final BulletinProjectionRepository bulletinRepository;
    private final CommentaireBulletinRepository commentaireRepository;

    public CommentaireBulletinService(BulletinProjectionRepository bulletinRepository, CommentaireBulletinRepository commentaireRepository) {
        this.bulletinRepository = bulletinRepository;
        this.commentaireRepository = commentaireRepository;
    }

    @Transactional
    public CommentaireBulletinDto.Profil genererPour(Long bulletinId) {
        BulletinProjection bulletin = bulletinRepository.findById(bulletinId)
                .orElseThrow(() -> new RessourceIntrouvableException("Bulletin", bulletinId));

        String texte = genererTexte(bulletin);

        CommentaireBulletin commentaire = commentaireRepository.findByBulletinId(bulletinId).orElseGet(CommentaireBulletin::new);
        commentaire.setBulletinId(bulletinId);
        commentaire.setCommentaireGenere(texte);
        commentaire.setValideParEnseignant(false); // toujours a re-valider apres regeneration
        commentaireRepository.save(commentaire);

        return versProfil(commentaire);
    }

    @Transactional
    public CommentaireBulletinDto.Profil valider(Long bulletinId, CommentaireBulletinDto.Validation requete) {
        CommentaireBulletin commentaire = commentaireRepository.findByBulletinId(bulletinId)
                .orElseThrow(() -> new RessourceIntrouvableException("CommentaireBulletin", bulletinId));
        commentaire.setCommentaireGenere(requete.commentaireFinal());
        commentaire.setValideParEnseignant(true);
        return versProfil(commentaire);
    }

    private String genererTexte(BulletinProjection bulletin) {
        // TODO production : remplacer par un appel a un modele de langage (contexte : moyenne,
        // rang, matieres fortes/faibles, tendance sur les periodes precedentes).
        BigDecimal moyenne = bulletin.getMoyenneGenerale() != null ? bulletin.getMoyenneGenerale() : BigDecimal.ZERO;
        String base;
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) {
            base = "Excellent trimestre. Serieux exemplaire, a encourager vivement a poursuivre sur cette voie.";
        } else if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) {
            base = "Bon trimestre. Resultats satisfaisants ; quelques efforts supplementaires permettraient d'exceller.";
        } else if (moyenne.compareTo(BigDecimal.valueOf(10)) >= 0) {
            base = "Trimestre moyen. Des capacites reelles, mais un travail plus regulier est necessaire.";
        } else if (moyenne.compareTo(BigDecimal.valueOf(8)) >= 0) {
            base = "Trimestre difficile. Un accompagnement rapproche est recommande pour eviter le decrochage.";
        } else {
            base = "Trimestre tres difficile. Une rencontre avec la famille est fortement recommandee.";
        }

        if (bulletin.getNombreAbsences() != null && bulletin.getNombreAbsences() > 3) {
            base += " " + bulletin.getNombreAbsences() + " absence(s) non justifiee(s) a signaler.";
        }
        return base;
    }

    private CommentaireBulletinDto.Profil versProfil(CommentaireBulletin c) {
        return new CommentaireBulletinDto.Profil(c.getId(), c.getBulletinId(), c.getCommentaireGenere(), c.isValideParEnseignant());
    }
}
