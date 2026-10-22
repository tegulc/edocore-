package com.educore.modules.auth.repository;

import com.educore.modules.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByModule(String module);
}
