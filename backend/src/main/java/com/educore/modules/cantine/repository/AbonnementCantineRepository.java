package com.educore.modules.cantine.repository;

import com.educore.modules.cantine.entity.AbonnementCantine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AbonnementCantineRepository extends JpaRepository<AbonnementCantine, Long> {
    List<AbonnementCantine> findByEleveId(Long eleveId);
    Optional<AbonnementCantine> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
}
