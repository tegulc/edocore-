package com.educore.modules.bibliotheque.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.bibliotheque.dto.EmpruntDto;
import com.educore.modules.bibliotheque.dto.LivreDto;
import com.educore.modules.bibliotheque.entity.Emprunt;
import com.educore.modules.bibliotheque.entity.Livre;
import com.educore.modules.bibliotheque.repository.EmpruntRepository;
import com.educore.modules.bibliotheque.repository.LivreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bibliotheque (Module 16). Regle 01 : jamais d'emprunt si exemplaires_disponibles
 * est deja a 0 (pas de reservation en attente dans cette premiere version).
 * Regle 02 : un retour restitue toujours l'exemplaire disponible, meme si
 * le retour est en retard (le retard est juste trace, pas bloquant en soi -
 * les amendes de bibliotheque sont hors perimetre de cette livraison).
 */
@Service
public class BibliothequeService {

    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;

    public BibliothequeService(LivreRepository livreRepository, EmpruntRepository empruntRepository) {
        this.livreRepository = livreRepository;
        this.empruntRepository = empruntRepository;
    }

    @Transactional
    public LivreDto.Profil ajouter(Long etablissementId, LivreDto.Creation requete) {
        Livre livre = new Livre();
        livre.setEtablissementId(etablissementId);
        livre.setTitre(requete.titre());
        livre.setAuteur(requete.auteur());
        livre.setEditeur(requete.editeur());
        livre.setCategorie(requete.categorie());
        livre.setIsbn(requete.isbn());
        livre.setNombreExemplaires(requete.nombreExemplaires());
        livre.setExemplairesDisponibles(requete.nombreExemplaires());
        livre.setQrCode(UUID.randomUUID().toString());
        livreRepository.save(livre);
        return versProfilLivre(livre);
    }

    public List<LivreDto.Profil> catalogue(Long etablissementId) {
        return livreRepository.findByEtablissementIdOrderByTitreAsc(etablissementId).stream().map(this::versProfilLivre).toList();
    }

    @Transactional
    public EmpruntDto.Profil emprunter(Long etablissementId, EmpruntDto.Creation requete) {
        Livre livre = trouverLivre(etablissementId, requete.livreId());
        if (livre.getExemplairesDisponibles() <= 0) {
            throw new BusinessException("AUCUN_EXEMPLAIRE_DISPONIBLE", "Tous les exemplaires de ce livre sont actuellement empruntes.", HttpStatus.CONFLICT);
        }

        livre.setExemplairesDisponibles(livre.getExemplairesDisponibles() - 1);

        Emprunt emprunt = new Emprunt();
        emprunt.setLivreId(livre.getId());
        emprunt.setUtilisateurId(requete.utilisateurId());
        emprunt.setDateRetourPrevue(requete.dateRetourPrevue());
        empruntRepository.save(emprunt);

        return versProfilEmprunt(emprunt, livre.getTitre());
    }

    @Transactional
    public EmpruntDto.Profil retourner(Long etablissementId, Long empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RessourceIntrouvableException("Emprunt", empruntId));
        Livre livre = trouverLivre(etablissementId, emprunt.getLivreId());

        if ("retourne".equals(emprunt.getStatut())) {
            throw new BusinessException("EMPRUNT_DEJA_RETOURNE", "Ce livre a deja ete retourne.", HttpStatus.CONFLICT);
        }
        emprunt.setStatut("retourne");
        emprunt.setDateRetourEffective(LocalDate.now());
        livre.setExemplairesDisponibles(livre.getExemplairesDisponibles() + 1);

        return versProfilEmprunt(emprunt, livre.getTitre());
    }

    public List<EmpruntDto.Profil> historiquePourUtilisateur(Long utilisateurId) {
        Map<Long, String> titres = livreRepository.findAll().stream().collect(Collectors.toMap(Livre::getId, Livre::getTitre));
        return empruntRepository.findByUtilisateurIdOrderByDateEmpruntDesc(utilisateurId).stream()
                .map(e -> versProfilEmprunt(e, titres.getOrDefault(e.getLivreId(), "?")))
                .toList();
    }

    private Livre trouverLivre(Long etablissementId, Long livreId) {
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RessourceIntrouvableException("Livre", livreId));
        if (!livre.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Livre", livreId);
        }
        return livre;
    }

    private LivreDto.Profil versProfilLivre(Livre l) {
        return new LivreDto.Profil(l.getId(), l.getIsbn(), l.getTitre(), l.getAuteur(), l.getEditeur(),
                l.getCategorie(), l.getNombreExemplaires(), l.getExemplairesDisponibles(), l.getQrCode());
    }

    private EmpruntDto.Profil versProfilEmprunt(Emprunt e, String titre) {
        return new EmpruntDto.Profil(e.getId(), e.getLivreId(), titre, e.getUtilisateurId(), e.getDateEmprunt(),
                e.getDateRetourPrevue(), e.getDateRetourEffective(), e.getStatut());
    }
}
