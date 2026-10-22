package com.educore.modules.transport.repository;

import com.educore.modules.transport.entity.EleveTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EleveTransportRepository extends JpaRepository<EleveTransport, Long> {
    Optional<EleveTransport> findByEleveId(Long eleveId);
}
