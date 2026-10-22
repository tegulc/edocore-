package com.educore.notifications.notification.repository;

import com.educore.notifications.notification.entity.AppareilMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppareilMobileRepository extends JpaRepository<AppareilMobile, Long> {
    List<AppareilMobile> findByUtilisateurId(Long utilisateurId);
}
