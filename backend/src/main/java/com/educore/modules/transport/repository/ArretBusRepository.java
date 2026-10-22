package com.educore.modules.transport.repository;

import com.educore.modules.transport.entity.ArretBus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArretBusRepository extends JpaRepository<ArretBus, Long> {
    List<ArretBus> findByItineraireIdOrderByOrdreAsc(Long itineraireId);
}
