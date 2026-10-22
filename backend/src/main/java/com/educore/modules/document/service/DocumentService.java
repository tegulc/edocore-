package com.educore.modules.document.service;

import com.educore.common.audit.JournalAuditService;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.document.dto.DocumentDto;
import com.educore.modules.document.entity.Document;
import com.educore.modules.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module 21 — Gestion documentaire. */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final JournalAuditService journalAuditService;

    public DocumentService(DocumentRepository documentRepository, JournalAuditService journalAuditService) {
        this.documentRepository = documentRepository;
        this.journalAuditService = journalAuditService;
    }

    @Transactional
    public DocumentDto.Profil archiver(Long etablissementId, Long utilisateurId, DocumentDto.Creation requete) {
        Document doc = new Document();
        doc.setEtablissementId(etablissementId);
        doc.setProprietaireType(requete.proprietaireType());
        doc.setProprietaireId(requete.proprietaireId());
        doc.setTypeDocument(requete.typeDocument());
        doc.setTitre(requete.titre());
        doc.setFichierUrl(requete.fichierUrl());
        doc.setAjouteParId(utilisateurId);
        documentRepository.save(doc);

        journalAuditService.enregistrer(etablissementId, utilisateurId, "CREATION", "documents", doc.getId(),
                null, null, null);
        return versProfil(doc);
    }

    public List<DocumentDto.Profil> lister(Long etablissementId, String proprietaireType, Long proprietaireId) {
        if (proprietaireType != null && proprietaireId != null) {
            return documentRepository.findByEtablissementIdAndProprietaireTypeAndProprietaireIdOrderByDateAjoutDesc(
                    etablissementId, proprietaireType, proprietaireId).stream().map(this::versProfil).toList();
        }
        return documentRepository.findByEtablissementIdOrderByDateAjoutDesc(etablissementId).stream()
                .map(this::versProfil).toList();
    }

    public DocumentDto.Profil obtenir(Long etablissementId, Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Document", id));
        if (!doc.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Document", id);
        }
        return versProfil(doc);
    }

    private DocumentDto.Profil versProfil(Document d) {
        return new DocumentDto.Profil(d.getId(), d.getProprietaireType(), d.getProprietaireId(),
                d.getTypeDocument(), d.getTitre(), d.getFichierUrl(), d.getArchive(), d.getDateAjout());
    }
}
