package com.educore.modules.bibliotheque.repository;

import com.educore.modules.bibliotheque.entity.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {
    List<Livre> findByEtablissementIdOrderByTitreAsc(Long etablissementId);
}
