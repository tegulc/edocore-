package com.educore.modules.transport.repository;

import com.educore.modules.transport.entity.Itineraire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItineraireRepository extends JpaRepository<Itineraire, Long> {
    List<Itineraire> findByBusId(Long busId);
}
