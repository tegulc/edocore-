package com.educore.modules.paiement.service;

import com.educore.modules.paiement.entity.Paiement;
import com.educore.modules.paiement.entity.Recu;
import com.educore.modules.paiement.repository.RecuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Genere un numero de recu sequentiel lisible (REC-{annee}-{sequence}). La generation du PDF lui-meme viendra avec le module Documents. */
@Service
public class RecuService {

    private final RecuRepository recuRepository;

    public RecuService(RecuRepository recuRepository) {
        this.recuRepository = recuRepository;
    }

    @Transactional
    public Recu genererPour(Paiement paiement) {
        String prefixe = "REC-%d-".formatted(LocalDate.now().getYear());
        long rang = recuRepository.countByNumeroRecuStartingWith(prefixe) + 1;

        Recu recu = new Recu();
        recu.setPaiementId(paiement.getId());
        recu.setNumeroRecu(prefixe + "%04d".formatted(rang));
        recuRepository.save(recu);
        return recu;
    }
}
