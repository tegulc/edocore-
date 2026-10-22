package com.educore.modules.discipline.repository;

import com.educore.modules.discipline.entity.TypeSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TypeSanctionRepository extends JpaRepository<TypeSanction, Long> {
    Optional<TypeSanction> findByCode(String code);
    List<TypeSanction> findAllByOrderByGraviteAsc();
}
