package com.educore.modules.note.repository;

import com.educore.modules.note.entity.MoyenneGenerale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MoyenneGeneraleRepository extends JpaRepository<MoyenneGenerale, Long> {
    Optional<MoyenneGenerale> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<MoyenneGenerale> findByClasseIdAndPeriodeId(Long classeId, Long periodeId);
    List<MoyenneGenerale> findByClasseIdAndPeriodeIdOrderByMoyenneGeneraleDesc(Long classeId, Long periodeId);
}
