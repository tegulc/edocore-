package com.educore.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Depot en ecriture (quasi) seule : pas de methode de suppression/mise a jour exposee (append-only). */
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    Page<JournalAudit> findByEtablissementIdOrderByHorodatageDesc(Long etablissementId, Pageable pageable);

    Page<JournalAudit> findByEtablissementIdAndTableCibleOrderByHorodatageDesc(Long etablissementId, String tableCible, Pageable pageable);
}
