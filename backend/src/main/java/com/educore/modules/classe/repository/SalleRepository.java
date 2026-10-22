package com.educore.modules.classe.repository;

import com.educore.modules.classe.entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByEtablissementIdOrderByNomAsc(Long etablissementId);
}
