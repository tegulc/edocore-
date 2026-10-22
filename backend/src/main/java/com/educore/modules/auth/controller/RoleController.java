package com.educore.modules.auth.controller;

import com.educore.modules.auth.entity.Permission;
import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.repository.PermissionRepository;
import com.educore.modules.auth.repository.RoleRepository;
import com.educore.modules.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Consultation des roles systeme et de leurs permissions (RBAC).
 * La creation de nouveaux roles "sur mesure" par etablissement est prevue
 * en Phase 2 avancee ; les 15 roles systeme du cahier des charges sont
 * fixes et non supprimables (voir Role.estSysteme).
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles & permissions", description = "Consultation du referentiel RBAC")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(summary = "Liste tous les roles systeme disponibles")
    public ResponseEntity<List<Role>> lister() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/permissions")
    @Operation(summary = "Liste toutes les permissions existantes, groupees par module")
    public ResponseEntity<List<Permission>> listerPermissions() {
        return ResponseEntity.ok(permissionRepository.findAll());
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Liste les codes de permission accordes a un role donne")
    public ResponseEntity<Set<String>> permissionsDuRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.listerCodesPermissions(roleId));
    }
}
