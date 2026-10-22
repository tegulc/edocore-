package com.educore.modules.paiement.repository;

import com.educore.modules.paiement.entity.Recu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecuRepository extends JpaRepository<Recu, Long> {
    long countByNumeroRecuStartingWith(String prefixe);
}
