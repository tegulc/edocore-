package com.educore.modules.auth.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.auth.entity.Role;
import com.educore.modules.auth.entity.UtilisateurRole;
import com.educore.modules.auth.repository.RoleRepository;
import com.educore.modules.auth.repository.UtilisateurRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests des regles metier du multi-role (cahier des charges Module 1 :
 * "attribution d'un role unique ou de roles multiples par utilisateur").
 */
class UtilisateurRoleServiceTest {

    @Mock private UtilisateurRoleRepository utilisateurRoleRepository;
    @Mock private RoleRepository roleRepository;

    private UtilisateurRoleService service;

    private Role roleProfesseur() {
        Role r = new Role(); r.setId(1L); r.setCode("PROFESSEUR"); return r;
    }

    private Role roleSurveillant() {
        Role r = new Role(); r.setId(2L); r.setCode("SURVEILLANT_GENERAL"); return r;
    }

    @BeforeEach
    void initialiser() {
        MockitoAnnotations.openMocks(this);
        service = new UtilisateurRoleService(utilisateurRoleRepository, roleRepository);
    }

    @Test
    void attribueLePremierRoleCommePrincipal() {
        when(roleRepository.findByCode("PROFESSEUR")).thenReturn(Optional.of(roleProfesseur()));
        when(roleRepository.findByCode("SURVEILLANT_GENERAL")).thenReturn(Optional.of(roleSurveillant()));

        service.attribuerRoles(100L, List.of("PROFESSEUR", "SURVEILLANT_GENERAL"));

        ArgumentCaptor<UtilisateurRole> captor = ArgumentCaptor.forClass(UtilisateurRole.class);
        org.mockito.Mockito.verify(utilisateurRoleRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        List<UtilisateurRole> sauvegardes = captor.getAllValues();
        assertThat(sauvegardes.get(0).getRoleId()).isEqualTo(1L);
        assertThat(sauvegardes.get(0).isEstPrincipal()).isTrue();
        assertThat(sauvegardes.get(1).getRoleId()).isEqualTo(2L);
        assertThat(sauvegardes.get(1).isEstPrincipal()).isFalse();
    }

    @Test
    void refuseUneListeDeRolesVide() {
        assertThatThrownBy(() -> service.attribuerRoles(100L, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "ROLE_REQUIS");
    }

    @Test
    void refuseDeRetirerLeDernierRoleDUnUtilisateur() {
        when(roleRepository.findByCode("PROFESSEUR")).thenReturn(Optional.of(roleProfesseur()));
        when(utilisateurRoleRepository.countByUtilisateurId(100L)).thenReturn(1L);

        assertThatThrownBy(() -> service.retirerRole(100L, "PROFESSEUR"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "DERNIER_ROLE");
    }

    @Test
    void refuseDAjouterUnRoleDejaAttribue() {
        when(roleRepository.findByCode("PROFESSEUR")).thenReturn(Optional.of(roleProfesseur()));
        when(utilisateurRoleRepository.existsByUtilisateurIdAndRoleId(100L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.ajouterRole(100L, "PROFESSEUR"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "ROLE_DEJA_ATTRIBUE");
    }

    @Test
    void reassigneAutomatiquementLeRolePrincipalQuandIlEstRetire() {
        Role professeur = roleProfesseur();
        UtilisateurRole associationPrincipale = new UtilisateurRole();
        associationPrincipale.setUtilisateurId(100L);
        associationPrincipale.setRoleId(1L);
        associationPrincipale.setEstPrincipal(true);

        UtilisateurRole associationSecondaire = new UtilisateurRole();
        associationSecondaire.setUtilisateurId(100L);
        associationSecondaire.setRoleId(2L);
        associationSecondaire.setEstPrincipal(false);

        when(roleRepository.findByCode("PROFESSEUR")).thenReturn(Optional.of(professeur));
        when(utilisateurRoleRepository.countByUtilisateurId(100L)).thenReturn(2L);
        when(utilisateurRoleRepository.findByUtilisateurIdAndEstPrincipalTrue(100L)).thenReturn(Optional.of(associationPrincipale));
        when(utilisateurRoleRepository.findByUtilisateurId(100L)).thenReturn(List.of(associationSecondaire));

        service.retirerRole(100L, "PROFESSEUR");

        org.mockito.Mockito.verify(utilisateurRoleRepository).deleteByUtilisateurIdAndRoleId(100L, 1L);
        assertThat(associationSecondaire.isEstPrincipal()).isTrue();
    }
}
