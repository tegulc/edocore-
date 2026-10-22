package com.educore.modules.bibliotheque.repository;

import com.educore.modules.bibliotheque.entity.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {
    List<Emprunt> findByUtilisateurIdOrderByDateEmpruntDesc(Long utilisateurId);
    long countByLivreIdAndStatut(Long livreId, String statut);
}
