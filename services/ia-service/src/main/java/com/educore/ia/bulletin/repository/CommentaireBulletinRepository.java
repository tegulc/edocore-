package com.educore.ia.bulletin.repository;

import com.educore.ia.bulletin.entity.CommentaireBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentaireBulletinRepository extends JpaRepository<CommentaireBulletin, Long> {
    Optional<CommentaireBulletin> findByBulletinId(Long bulletinId);
}
