package com.educore.modules.paiement.service;

import com.educore.modules.comptabilite.service.EcritureComptableService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.paiement.adapter.ProviderPaiement;
import com.educore.modules.paiement.dto.PaiementDto;
import com.educore.modules.paiement.entity.CleIdempotence;
import com.educore.modules.paiement.entity.MoyenPaiement;
import com.educore.modules.paiement.entity.Paiement;
import com.educore.modules.paiement.repository.CleIdempotenceRepository;
import com.educore.modules.paiement.repository.MoyenPaiementRepository;
import com.educore.modules.paiement.repository.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifie la regle d'idempotence sur POST /paiements/initier (Volume 6, Partie
 * IV.3) : rejouer la meme Idempotency-Key ne doit JAMAIS creer un second
 * paiement, meme si le client retente suite a un timeout reseau.
 */
class PaiementServiceTest {

    @Mock private PaiementRepository paiementRepository;
    @Mock private MoyenPaiementRepository moyenPaiementRepository;
    @Mock private CleIdempotenceRepository cleIdempotenceRepository;
    @Mock private EcheancierService echeancierService;
    @Mock private RecuService recuService;
    @Mock private EleveService eleveService;
    @Mock private EcritureComptableService ecritureComptableService;
    @Mock private ProviderPaiement orangeMoneyAdapter;

    private PaiementService service;

    @BeforeEach
    void initialiser() {
        MockitoAnnotations.openMocks(this);
        when(orangeMoneyAdapter.code()).thenReturn("orange_money");
        service = new PaiementService(paiementRepository, moyenPaiementRepository, cleIdempotenceRepository,
                echeancierService, recuService, eleveService, ecritureComptableService, List.of(orangeMoneyAdapter));
    }

    @Test
    void rejouerLaMemeCleDIdempotenceNeCreeJamaisUnSecondPaiement() {
        Paiement paiementExistant = new Paiement();
        paiementExistant.setId(42L);
        paiementExistant.setStatut("en_attente");

        CleIdempotence cle = new CleIdempotence();
        cle.setPaiementId(42L);
        when(cleIdempotenceRepository.findByEtablissementIdAndCle(1L, "cle-abc")).thenReturn(Optional.of(cle));
        when(paiementRepository.findById(42L)).thenReturn(Optional.of(paiementExistant));

        var requete = new PaiementDto.Initiation(10L, null, BigDecimal.valueOf(25000), "orange_money", "699000000");
        var reponse = service.initier(1L, "cle-abc", requete);

        assertThat(reponse.paiementId()).isEqualTo(42L);
        // Regle centrale : aucun nouveau paiement ni nouvelle cle ne doivent etre crees au second appel.
        verify(paiementRepository, never()).save(any());
        verify(cleIdempotenceRepository, never()).save(any());
    }

    @Test
    void premierAppelCreeUnPaiementEtSaCleDIdempotence() {
        when(cleIdempotenceRepository.findByEtablissementIdAndCle(1L, "cle-nouvelle")).thenReturn(Optional.empty());

        MoyenPaiement moyen = new MoyenPaiement();
        moyen.setId(5L);
        moyen.setCode("orange_money");
        when(moyenPaiementRepository.findByCode("orange_money")).thenReturn(Optional.of(moyen));

        when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> {
            Paiement p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(orangeMoneyAdapter.initier(any(), any(), any())).thenReturn("Confirmez la transaction sur votre telephone.");

        var requete = new PaiementDto.Initiation(10L, null, BigDecimal.valueOf(25000), "orange_money", "699000000");
        var reponse = service.initier(1L, "cle-nouvelle", requete);

        assertThat(reponse.paiementId()).isEqualTo(99L);
        assertThat(reponse.statut()).isEqualTo("en_attente");
        assertThat(reponse.instructions()).isEqualTo("Confirmez la transaction sur votre telephone.");

        ArgumentCaptor<CleIdempotence> capture = ArgumentCaptor.forClass(CleIdempotence.class);
        verify(cleIdempotenceRepository).save(capture.capture());
        assertThat(capture.getValue().getCle()).isEqualTo("cle-nouvelle");
        assertThat(capture.getValue().getPaiementId()).isEqualTo(99L);
    }

    @Test
    void unWebhookRejoueNeConfirmePasDeuxFoisLeMemePaiement() {
        Paiement paiementDejaConfirme = new Paiement();
        paiementDejaConfirme.setId(7L);
        paiementDejaConfirme.setStatut("confirme");
        when(paiementRepository.findById(7L)).thenReturn(Optional.of(paiementDejaConfirme));

        service.traiterWebhook(7L, "TXN-999", "SUCCESS");

        // Deja confirme : aucun recalcul de tranche, aucune nouvelle ecriture comptable, aucun nouveau recu.
        verifyNoInteractions(echeancierService, recuService, ecritureComptableService);
    }
}
