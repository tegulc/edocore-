package com.educore.modules.note.repository;

import com.educore.modules.note.entity.MoyenneMatiere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MoyenneMatiereRepository extends JpaRepository<MoyenneMatiere, Long> {
    Optional<MoyenneMatiere> findByEleveIdAndClasseMatiereIdAndPeriodeId(Long eleveId, Long classeMatiereId, Long periodeId);
    List<MoyenneMatiere> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<MoyenneMatiere> findByClasseMatiereIdAndPeriodeId(Long classeMatiereId, Long periodeId);
}
