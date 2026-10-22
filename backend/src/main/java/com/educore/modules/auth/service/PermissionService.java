package com.educore.modules.auth.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.auth.entity.Permission;
import com.educore.modules.auth.repository.PermissionRepository;
import com.educore.modules.auth.repository.RolePermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolution et verification des permissions fines d'un ou plusieurs roles
 * (RBAC, voir document d'architecture section 8.2). Complementaire au
 * controle macro @PreAuthorize("hasRole(...)") deja en place dans
 * SecurityConfig : ce service permet une verification metier plus precise
 * a l'interieur d'un Service (ex : un PROFESSEUR peut avoir NOTES_SAISIR
 * sans avoir NOTES_VALIDER).
 *
 * Support multi-role (cahier des charges Module 1) : les permissions de
 * TOUS les roles attribues a l'utilisateur sont cumulees (union), jamais
 * seulement celles du role principal.
 */
@Service
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public PermissionService(RolePermissionRepository rolePermissionRepository, PermissionRepository permissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    /** Retourne l'ensemble des codes de permission accordes a un role (mis en cache Redis en Phase 2+). */
    public Set<String> listerCodesPermissions(Long roleId) {
        return listerCodesPermissionsCumulees(List.of(roleId));
    }

    /** Union des permissions accordees par l'ensemble des roles fournis (cas multi-role). */
    public Set<String> listerCodesPermissionsCumulees(Collection<Long> roleIds) {
        List<Long> permissionIds = roleIds.stream()
                .flatMap(roleId -> rolePermissionRepository.findByRoleId(roleId).stream())
                .map(rp -> rp.getPermissionId())
                .distinct()
                .toList();
        return permissionRepository.findAllById(permissionIds).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /** Leve une exception 403 si aucun des roles fournis ne possede la permission demandee. */
    public void exigerPermission(Collection<Long> roleIds, String codePermission) {
        if (!listerCodesPermissionsCumulees(roleIds).contains(codePermission)) {
            throw new BusinessException("PERMISSION_MANQUANTE",
                    "Cette action necessite la permission : " + codePermission, HttpStatus.FORBIDDEN);
        }
    }
}
