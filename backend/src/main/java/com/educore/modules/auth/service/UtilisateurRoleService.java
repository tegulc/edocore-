package com.educore.modules.auth.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.entity.UtilisateurRole;
import com.educore.modules.auth.repository.RoleRepository;
import com.educore.modules.auth.repository.UtilisateurRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Source unique de verite pour l'attribution des roles a un utilisateur
 * (cahier des charges Module 1 : "attribution d'un role unique ou de roles
 * multiples par utilisateur"). Utilise a la fois par AuthService (connexion,
 * construction du JWT) et par UtilisateurAdminService (creation/gestion des
 * comptes), pour ne jamais dupliquer cette regle metier.
 */
@Service
public class UtilisateurRoleService {

    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final RoleRepository roleRepository;

    public UtilisateurRoleService(UtilisateurRoleRepository utilisateurRoleRepository, RoleRepository roleRepository) {
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.roleRepository = roleRepository;
    }

    /** Attribue un ensemble de roles a un utilisateur. Le premier code de la liste devient le role principal. */
    @Transactional
    public void attribuerRoles(Long utilisateurId, List<String> codesRoles) {
        if (codesRoles == null || codesRoles.isEmpty()) {
            throw new BusinessException("ROLE_REQUIS", "Un utilisateur doit avoir au moins un role.", HttpStatus.BAD_REQUEST);
        }
        for (int i = 0; i < codesRoles.size(); i++) {
            final String codeRole = codesRoles.get(i);
            Role role = roleRepository.findByCode(codeRole)
                    .orElseThrow(() -> new BusinessException("ROLE_INCONNU", "Role inconnu : " + codeRole, HttpStatus.BAD_REQUEST));

            UtilisateurRole association = new UtilisateurRole();
            association.setUtilisateurId(utilisateurId);
            association.setRoleId(role.getId());
            association.setEstPrincipal(i == 0);
            utilisateurRoleRepository.save(association);
        }
    }

    /** Ajoute un role supplementaire a un utilisateur qui en possede deja au moins un. */
    @Transactional
    public void ajouterRole(Long utilisateurId, String codeRole) {
        Role role = roleRepository.findByCode(codeRole)
                .orElseThrow(() -> new BusinessException("ROLE_INCONNU", "Role inconnu : " + codeRole, HttpStatus.BAD_REQUEST));

        if (utilisateurRoleRepository.existsByUtilisateurIdAndRoleId(utilisateurId, role.getId())) {
            throw new BusinessException("ROLE_DEJA_ATTRIBUE", "Ce role est deja attribue a cet utilisateur.", HttpStatus.CONFLICT);
        }
        UtilisateurRole association = new UtilisateurRole();
        association.setUtilisateurId(utilisateurId);
        association.setRoleId(role.getId());
        association.setEstPrincipal(utilisateurRoleRepository.countByUtilisateurId(utilisateurId) == 0);
        utilisateurRoleRepository.save(association);
    }

    /** Retire un role. Refuse de retirer le dernier role restant (un utilisateur doit toujours en avoir au moins un). */
    @Transactional
    public void retirerRole(Long utilisateurId, String codeRole) {
        Role role = roleRepository.findByCode(codeRole)
                .orElseThrow(() -> new BusinessException("ROLE_INCONNU", "Role inconnu : " + codeRole, HttpStatus.BAD_REQUEST));

        if (utilisateurRoleRepository.countByUtilisateurId(utilisateurId) <= 1) {
            throw new BusinessException("DERNIER_ROLE", "Impossible de retirer le dernier role d'un utilisateur.", HttpStatus.CONFLICT);
        }
        boolean etaitPrincipal = utilisateurRoleRepository.findByUtilisateurIdAndEstPrincipalTrue(utilisateurId)
                .map(ur -> ur.getRoleId().equals(role.getId())).orElse(false);

        utilisateurRoleRepository.deleteByUtilisateurIdAndRoleId(utilisateurId, role.getId());

        if (etaitPrincipal) {
            // Reassigne automatiquement le role principal a l'un des roles restants.
            utilisateurRoleRepository.findByUtilisateurId(utilisateurId).stream().findFirst()
                    .ifPresent(ur -> { ur.setEstPrincipal(true); utilisateurRoleRepository.save(ur); });
        }
    }

    @Transactional
    public void definirRolePrincipal(Long utilisateurId, String codeRole) {
        Role role = roleRepository.findByCode(codeRole)
                .orElseThrow(() -> new BusinessException("ROLE_INCONNU", "Role inconnu : " + codeRole, HttpStatus.BAD_REQUEST));
        if (!utilisateurRoleRepository.existsByUtilisateurIdAndRoleId(utilisateurId, role.getId())) {
            throw new BusinessException("ROLE_NON_ATTRIBUE", "Ce role n'est pas attribue a cet utilisateur.", HttpStatus.BAD_REQUEST);
        }
        utilisateurRoleRepository.retirerPrincipalDeTousLesRoles(utilisateurId);
        utilisateurRoleRepository.findByUtilisateurId(utilisateurId).stream()
                .filter(ur -> ur.getRoleId().equals(role.getId())).findFirst()
                .ifPresent(ur -> { ur.setEstPrincipal(true); utilisateurRoleRepository.save(ur); });
    }

    /** Remplace integralement l'ensemble des roles d'un utilisateur (utilise par UtilisateurAdminService.modifier). */
    @Transactional
    public void remplacerRoles(Long utilisateurId, List<String> nouveauxCodesRoles) {
        utilisateurRoleRepository.deleteByUtilisateurId(utilisateurId);
        attribuerRoles(utilisateurId, nouveauxCodesRoles);
    }

    /** Tous les roles (entites completes) attribues a un utilisateur. */
    public List<Role> listerRoles(Long utilisateurId) {
        List<Long> roleIds = utilisateurRoleRepository.findByUtilisateurId(utilisateurId).stream()
                .map(UtilisateurRole::getRoleId).toList();
        return roleRepository.findAllById(roleIds);
    }

    /** Codes des roles attribues (raccourci pratique pour le JWT/DTO). */
    public Set<String> listerCodesRoles(Long utilisateurId) {
        return listerRoles(utilisateurId).stream().map(Role::getCode).collect(Collectors.toSet());
    }

    /** Le role principal (contexte JWT/UI par defaut). Repli sur le premier role trouve si aucun n'est marque principal. */
    public Role obtenirRolePrincipal(Long utilisateurId) {
        Long roleId = utilisateurRoleRepository.findByUtilisateurIdAndEstPrincipalTrue(utilisateurId)
                .map(UtilisateurRole::getRoleId)
                .or(() -> utilisateurRoleRepository.findByUtilisateurId(utilisateurId).stream().findFirst().map(UtilisateurRole::getRoleId))
                .orElseThrow(() -> new BusinessException("AUCUN_ROLE", "Cet utilisateur n'a aucun role attribue.", HttpStatus.CONFLICT));
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalStateException("Role introuvable : " + roleId));
    }

    /** Tous les identifiants de role, pour l'agregation des permissions (PermissionService). */
    public List<Long> listerIdsRoles(Long utilisateurId) {
        return utilisateurRoleRepository.findByUtilisateurId(utilisateurId).stream()
                .map(UtilisateurRole::getRoleId).toList();
    }
}
