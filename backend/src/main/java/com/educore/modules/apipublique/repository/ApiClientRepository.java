package com.educore.modules.apipublique.repository;

import com.educore.modules.apipublique.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {
    Optional<ApiClient> findByClientId(String clientId);
    List<ApiClient> findByEtablissementId(Long etablissementId);
}
