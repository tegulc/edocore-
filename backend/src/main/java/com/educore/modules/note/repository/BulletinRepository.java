package com.educore.modules.note.repository;

import com.educore.modules.note.entity.Bulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BulletinRepository extends JpaRepository<Bulletin, Long> {
    Optional<Bulletin> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<Bulletin> findByClasseIdAndPeriodeId(Long classeId, Long periodeId);
}
