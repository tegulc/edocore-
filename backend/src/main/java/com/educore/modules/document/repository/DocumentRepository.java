package com.educore.modules.document.repository;

import com.educore.modules.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEtablissementIdAndProprietaireTypeAndProprietaireIdOrderByDateAjoutDesc(
            Long etablissementId, String proprietaireType, Long proprietaireId);
    List<Document> findByEtablissementIdAndTypeDocumentOrderByDateAjoutDesc(Long etablissementId, String typeDocument);
    List<Document> findByEtablissementIdOrderByDateAjoutDesc(Long etablissementId);
}
