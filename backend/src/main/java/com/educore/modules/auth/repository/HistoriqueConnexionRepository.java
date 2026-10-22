package com.educore.modules.auth.repository;

import com.educore.modules.auth.entity.HistoriqueConnexion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Depot en ecriture seule (append-only) : aucune methode de suppression/mise a jour n'est ajoutee volontairement. */
public interface HistoriqueConnexionRepository extends JpaRepository<HistoriqueConnexion, Long> {
}
