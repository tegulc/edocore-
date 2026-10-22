package com.educore.modules.parent.repository;

import com.educore.modules.parent.entity.EleveParent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EleveParentRepository extends JpaRepository<EleveParent, Long> {
    List<EleveParent> findByEleveId(Long eleveId);
    List<EleveParent> findByParentId(Long parentId);
    Optional<EleveParent> findByEleveIdAndParentId(Long eleveId, Long parentId);
}
