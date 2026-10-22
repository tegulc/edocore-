package com.educore.modules.rapport.service;

import com.educore.modules.note.entity.MoyenneGenerale;
import com.educore.modules.note.repository.MoyenneGeneraleRepository;
import com.educore.modules.paiement.service.PaiementService;
import com.educore.modules.personnel.repository.DemandeCongeRepository;
import com.educore.modules.presence.service.PresenceService;
import com.educore.modules.rapport.dto.RapportDto;
import com.educore.modules.eleve.repository.EleveRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Module 27 — Rapports statistiques (vues SQL + repositories existants). */
@Service
public class RapportService {

    private final JdbcTemplate jdbcTemplate;
    private final MoyenneGeneraleRepository moyenneGeneraleRepository;
    private final EleveRepository eleveRepository;
    private final PaiementService paiementService;
    private final PresenceService presenceService;
    private final DemandeCongeRepository congeRepository;

    public RapportService(JdbcTemplate jdbcTemplate, MoyenneGeneraleRepository moyenneGeneraleRepository,
                           EleveRepository eleveRepository, PaiementService paiementService,
                           PresenceService presenceService, DemandeCongeRepository congeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.moyenneGeneraleRepository = moyenneGeneraleRepository;
        this.eleveRepository = eleveRepository;
        this.paiementService = paiementService;
        this.presenceService = presenceService;
        this.congeRepository = congeRepository;
    }

    public List<RapportDto.EffectifClasse> effectifsParClasse(Long etablissementId) {
        return jdbcTemplate.query("""
                SELECT v.classe_id, v.libelle, v.effectif, v.capacite
                FROM vue_effectifs_classe v
                JOIN classes c ON c.id = v.classe_id
                WHERE c.etablissement_id = ?
                ORDER BY v.libelle
                """, (rs, rowNum) -> new RapportDto.EffectifClasse(
                rs.getLong("classe_id"), rs.getString("libelle"),
                rs.getLong("effectif"), rs.getInt("capacite")), etablissementId);
    }

    public List<RapportDto.SoldeEleve> soldesEleves(Long etablissementId) {
        return jdbcTemplate.query("""
                SELECT e.id AS eleve_id, e.nom, e.prenom,
                       COALESCE(v.total_du, 0) AS total_du,
                       COALESCE(v.total_paye, 0) AS total_paye,
                       COALESCE(v.solde_restant, 0) AS solde_restant
                FROM eleves e
                LEFT JOIN vue_solde_eleve v ON v.eleve_id = e.id
                WHERE e.etablissement_id = ? AND e.statut = 'actif'
                ORDER BY v.solde_restant DESC NULLS LAST
                """, (rs, rowNum) -> new RapportDto.SoldeEleve(
                rs.getLong("eleve_id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getBigDecimal("total_du"), rs.getBigDecimal("total_paye"), rs.getBigDecimal("solde_restant")),
                etablissementId);
    }

    public List<RapportDto.TauxPresenceEleve> tauxPresences(Long etablissementId) {
        return jdbcTemplate.query("""
                SELECT e.id AS eleve_id, e.nom, e.prenom,
                       COALESCE(v.jours_presents, 0) AS jours_presents,
                       COALESCE(v.jours_absents, 0) AS jours_absents,
                       COALESCE(v.taux_presence, 0) AS taux_presence
                FROM eleves e
                LEFT JOIN vue_taux_presence_eleve v ON v.eleve_id = e.id
                WHERE e.etablissement_id = ? AND e.statut = 'actif'
                ORDER BY v.taux_presence ASC NULLS LAST
                """, (rs, rowNum) -> new RapportDto.TauxPresenceEleve(
                rs.getLong("eleve_id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getLong("jours_presents"), rs.getLong("jours_absents"), rs.getDouble("taux_presence")),
                etablissementId);
    }

    public List<RapportDto.MoyenneClasse> moyennesClasse(Long etablissementId, Long classeId, Long periodeId) {
        List<MoyenneGenerale> moyennes = moyenneGeneraleRepository
                .findByClasseIdAndPeriodeIdOrderByMoyenneGeneraleDesc(classeId, periodeId);
        return moyennes.stream()
                .filter(m -> eleveRepository.findById(m.getEleveId())
                        .map(e -> e.getEtablissementId().equals(etablissementId)).orElse(false))
                .map(m -> {
                    var eleve = eleveRepository.findById(m.getEleveId()).orElseThrow();
                    return new RapportDto.MoyenneClasse(m.getEleveId(), eleve.getNom(), eleve.getPrenom(),
                            m.getMoyenneGenerale(), m.getRangClasse());
                }).toList();
    }

    public RapportDto.Synthese synthese(Long etablissementId) {
        long elevesActifs = eleveRepository.countByEtablissementIdAndStatut(etablissementId, "actif");
        double tauxPresence = presenceService.tauxPresenceDuJour(etablissementId, LocalDate.now());
        BigDecimal recettes = paiementService.sommeConfirmeeCeMois(etablissementId);

        Long incidents = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM incidents_disciplinaires i
                JOIN eleves e ON e.id = i.eleve_id
                WHERE e.etablissement_id = ?
                """, Long.class, etablissementId);

        long congesEnAttente = congeRepository.findByStatutOrderByCreatedAtDesc("en_attente").stream()
                .filter(c -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM utilisateurs WHERE id = ? AND etablissement_id = ?",
                        Long.class, c.getUtilisateurId(), etablissementId) > 0)
                .count();

        return new RapportDto.Synthese(elevesActifs, tauxPresence >= 0 ? tauxPresence : 0,
                recettes, incidents != null ? incidents : 0, congesEnAttente);
    }
}
