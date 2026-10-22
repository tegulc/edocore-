package com.educore.modules.eleve.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.classe.dto.ClasseDto;
import com.educore.modules.classe.service.ClasseService;
import com.educore.modules.eleve.dto.InscriptionDto;
import com.educore.modules.eleve.entity.Eleve;
import com.educore.modules.eleve.repository.InscriptionRepository;
import com.educore.modules.etablissement.dto.AnneeScolaireDto;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Verifie les regles metier d'inscription : pas de doublon par annee, jamais au-dela de la capacite de la classe. */
class InscriptionServiceTest {

    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private EleveService eleveService;
    @Mock private ClasseService classeService;
    @Mock private AnneeScolaireService anneeScolaireService;

    private InscriptionService service;

    @BeforeEach
    void initialiser() {
        MockitoAnnotations.openMocks(this);
        service = new InscriptionService(inscriptionRepository, eleveService, classeService, anneeScolaireService);

        Eleve eleve = new Eleve();
        eleve.setId(1L);
        when(eleveService.trouver(10L, 1L)).thenReturn(eleve);

        var anneeActive = new AnneeScolaireDto.Profil(100L, "2025-2026", LocalDate.now(), LocalDate.now().plusMonths(10), true);
        when(anneeScolaireService.obtenirActive(10L)).thenReturn(anneeActive);
    }

    @Test
    void refuseUneSecondeInscriptionPourLaMemeAnnee() {
        when(inscriptionRepository.findByEleveIdAndAnneeScolaireId(1L, 100L))
                .thenReturn(Optional.of(new com.educore.modules.eleve.entity.Inscription()));

        var requete = new InscriptionDto.Creation(5L, null);

        assertThatThrownBy(() -> service.inscrire(10L, 1L, requete))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "DEJA_INSCRIT");
    }

    @Test
    void refuseUneInscriptionSiLaClasseEstComplete() {
        when(inscriptionRepository.findByEleveIdAndAnneeScolaireId(1L, 100L)).thenReturn(Optional.empty());
        var classePleine = new ClasseDto.Profil(5L, 100L, 1L, "3e", null, null, null, null, "3eme A1", null, null, 60, 60L);
        when(classeService.obtenir(10L, 5L)).thenReturn(classePleine);

        var requete = new InscriptionDto.Creation(5L, null);

        assertThatThrownBy(() -> service.inscrire(10L, 1L, requete))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "CLASSE_COMPLETE");
    }

    @Test
    void accepteLInscriptionSiDeLaPlaceReste() {
        when(inscriptionRepository.findByEleveIdAndAnneeScolaireId(1L, 100L)).thenReturn(Optional.empty());
        var classeAvecPlace = new ClasseDto.Profil(5L, 100L, 1L, "3e", null, null, null, null, "3eme A1", null, null, 60, 45L);
        when(classeService.obtenir(10L, 5L)).thenReturn(classeAvecPlace);

        var requete = new InscriptionDto.Creation(5L, null);
        var resultat = service.inscrire(10L, 1L, requete);

        assertThat(resultat.classeId()).isEqualTo(5L);
        assertThat(resultat.statut()).isEqualTo("validee");
    }
}
