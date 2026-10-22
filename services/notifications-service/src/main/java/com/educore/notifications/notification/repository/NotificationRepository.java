package com.educore.notifications.notification.repository;

import com.educore.notifications.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(Long destinataireId);
    long countByDestinataireIdAndLuLeIsNull(Long destinataireId);
}
