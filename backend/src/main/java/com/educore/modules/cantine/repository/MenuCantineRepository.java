package com.educore.modules.cantine.repository;

import com.educore.modules.cantine.entity.MenuCantine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MenuCantineRepository extends JpaRepository<MenuCantine, Long> {
    List<MenuCantine> findByEtablissementIdAndDateJourBetweenOrderByDateJourAsc(Long etablissementId, LocalDate debut, LocalDate fin);
}
