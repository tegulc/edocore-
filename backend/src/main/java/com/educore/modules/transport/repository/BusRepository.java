package com.educore.modules.transport.repository;

import com.educore.modules.transport.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByEtablissementId(Long etablissementId);
}
