package com.educore.ia.bulletin.repository;

import com.educore.ia.bulletin.entity.BulletinProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulletinProjectionRepository extends JpaRepository<BulletinProjection, Long> {
}
