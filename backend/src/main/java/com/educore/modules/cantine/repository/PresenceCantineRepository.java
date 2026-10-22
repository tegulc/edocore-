package com.educore.modules.cantine.repository;

import com.educore.modules.cantine.entity.PresenceCantine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PresenceCantineRepository extends JpaRepository<PresenceCantine, Long> {
    Optional<PresenceCantine> findByEleveIdAndDateJour(Long eleveId, LocalDate dateJour);
}
