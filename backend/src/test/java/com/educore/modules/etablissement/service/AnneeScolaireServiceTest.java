package com.educore.modules.etablissement.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.etablissement.dto.AnneeScolaireDto;
import com.educore.modules.etablissement.entity.AnneeScolaire;
import com.educore.modules.etablissement.repository.AnneeScolaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Verifie la regle metier centrale : une seule annee scolaire active a la fois par etablissement. */
class AnneeScolaireServiceTest {

    @Mock private AnneeScolaireRepository anneeScolaireRepository;
    private AnneeScolaireService service;

    @BeforeEach
    void initialiser() {
        MockitoAnnotations.openMocks(this);
        service = new AnneeScolaireService(anneeScolaireRepository);
    }

    @Test
    void laPremiereAnneeCreeeEstActiveeAutomatiquement() {
        when(anneeScolaireRepository.findByEtablissementIdAndEstActiveTrue(1L)).thenReturn(Optional.empty());
        when(anneeScolaireRepository.existsByEtablissementIdAndLibelle(1L, "2025-2026")).thenReturn(false);

        var requete = new AnneeScolaireDto.Creation("2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 7, 31));
        var profil = service.creer(1L, requete);

        assertThat(profil.estActive()).isTrue();
    }

    @Test
    void laSecondeAnneeCreeeNEstPasActiveeAutomatiquement() {
        AnneeScolaire anneeExistante = new AnneeScolaire();
        anneeExistante.setEstActive(true);
        when(anneeScolaireRepository.findByEtablissementIdAndEstActiveTrue(1L)).thenReturn(Optional.of(anneeExistante));
        when(anneeScolaireRepository.existsByEtablissementIdAndLibelle(1L, "2026-2027")).thenReturn(false);

        var requete = new AnneeScolaireDto.Creation("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31));
        var profil = service.creer(1L, requete);

        assertThat(profil.estActive()).isFalse();
    }

    @Test
    void activerUneAnneeDesactiveTouteAutreAnneeDuMemeEtablissement() {
        AnneeScolaire annee = new AnneeScolaire();
        annee.setId(2L);
        annee.setEtablissementId(1L);
        annee.setEstActive(false);
        when(anneeScolaireRepository.findById(2L)).thenReturn(Optional.of(annee));

        var profil = service.activer(1L, 2L);

        verify(anneeScolaireRepository).desactiverToutesLesAnnees(1L);
        assertThat(profil.estActive()).isTrue();
    }

    @Test
    void refuseDActiverUneAnneeAppartenantAUnAutreEtablissement() {
        AnneeScolaire annee = new AnneeScolaire();
        annee.setId(2L);
        annee.setEtablissementId(99L); // autre etablissement
        when(anneeScolaireRepository.findById(2L)).thenReturn(Optional.of(annee));

        assertThatThrownBy(() -> service.activer(1L, 2L))
                .hasMessageContaining("AnneeScolaire");
        verify(anneeScolaireRepository, never()).desactiverToutesLesAnnees(anyLong());
    }

    @Test
    void refuseUneAnneeDontLaDateDeFinPrecedeLaDateDeDebut() {
        var requete = new AnneeScolaireDto.Creation("2025-2026", LocalDate.of(2026, 7, 31), LocalDate.of(2025, 9, 1));

        assertThatThrownBy(() -> service.creer(1L, requete))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "PERIODE_INVALIDE");
    }
}
