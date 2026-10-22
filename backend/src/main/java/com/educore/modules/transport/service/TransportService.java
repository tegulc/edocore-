package com.educore.modules.transport.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.transport.dto.TransportDto;
import com.educore.modules.transport.entity.ArretBus;
import com.educore.modules.transport.entity.Bus;
import com.educore.modules.transport.entity.EleveTransport;
import com.educore.modules.transport.entity.Itineraire;
import com.educore.modules.transport.repository.ArretBusRepository;
import com.educore.modules.transport.repository.BusRepository;
import com.educore.modules.transport.repository.EleveTransportRepository;
import com.educore.modules.transport.repository.ItineraireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Transport scolaire (Module 18). Le suivi GPS temps reel et le pointage embarque (presences_bus) sont hors perimetre de cette livraison. */
@Service
public class TransportService {

    private final BusRepository busRepository;
    private final ItineraireRepository itineraireRepository;
    private final ArretBusRepository arretBusRepository;
    private final EleveTransportRepository eleveTransportRepository;
    private final EleveService eleveService;

    public TransportService(BusRepository busRepository, ItineraireRepository itineraireRepository, ArretBusRepository arretBusRepository,
                             EleveTransportRepository eleveTransportRepository, EleveService eleveService) {
        this.busRepository = busRepository;
        this.itineraireRepository = itineraireRepository;
        this.arretBusRepository = arretBusRepository;
        this.eleveTransportRepository = eleveTransportRepository;
        this.eleveService = eleveService;
    }

    @Transactional
    public TransportDto.BusProfil creerBus(Long etablissementId, TransportDto.BusCreation requete) {
        Bus bus = new Bus();
        bus.setEtablissementId(etablissementId);
        bus.setImmatriculation(requete.immatriculation());
        bus.setCapacite(requete.capacite());
        bus.setChauffeurId(requete.chauffeurId());
        busRepository.save(bus);
        return versProfilBus(bus);
    }

    public List<TransportDto.BusProfil> listerBus(Long etablissementId) {
        return busRepository.findByEtablissementId(etablissementId).stream().map(this::versProfilBus).toList();
    }

    @Transactional
    public TransportDto.ItineraireProfil creerItineraire(Long etablissementId, TransportDto.ItineraireCreation requete) {
        Bus bus = trouverBus(etablissementId, requete.busId());
        Itineraire itineraire = new Itineraire();
        itineraire.setBusId(bus.getId());
        itineraire.setLibelle(requete.libelle());
        itineraire.setHeureDepartMatin(requete.heureDepartMatin());
        itineraire.setHeureRetourSoir(requete.heureRetourSoir());
        itineraireRepository.save(itineraire);
        return versProfilItineraire(itineraire);
    }

    @Transactional
    public TransportDto.ArretProfil ajouterArret(Long itineraireId, TransportDto.ArretCreation requete) {
        ArretBus arret = new ArretBus();
        arret.setItineraireId(itineraireId);
        arret.setLibelle(requete.libelle());
        if (requete.latitude() != null) arret.setLatitude(BigDecimal.valueOf(requete.latitude()));
        if (requete.longitude() != null) arret.setLongitude(BigDecimal.valueOf(requete.longitude()));
        arret.setOrdre(requete.ordre());
        arretBusRepository.save(arret);
        return versProfilArret(arret);
    }

    public List<TransportDto.ArretProfil> arretsDeLItineraire(Long itineraireId) {
        return arretBusRepository.findByItineraireIdOrderByOrdreAsc(itineraireId).stream().map(this::versProfilArret).toList();
    }

    @Transactional
    public TransportDto.AffectationProfil affecterEleve(Long etablissementId, Long eleveId, TransportDto.AffectationEleve requete) {
        eleveService.trouver(etablissementId, eleveId);
        EleveTransport affectation = eleveTransportRepository.findByEleveId(eleveId).orElseGet(EleveTransport::new);
        affectation.setEleveId(eleveId);
        affectation.setItineraireId(requete.itineraireId());
        affectation.setArretId(requete.arretId());
        eleveTransportRepository.save(affectation);
        return new TransportDto.AffectationProfil(affectation.getId(), eleveId, affectation.getItineraireId(), affectation.getArretId());
    }

    private Bus trouverBus(Long etablissementId, Long busId) {
        Bus bus = busRepository.findById(busId).orElseThrow(() -> new RessourceIntrouvableException("Bus", busId));
        if (!bus.getEtablissementId().equals(etablissementId)) throw new RessourceIntrouvableException("Bus", busId);
        return bus;
    }

    private TransportDto.BusProfil versProfilBus(Bus b) {
        return new TransportDto.BusProfil(b.getId(), b.getImmatriculation(), b.getCapacite(), b.getChauffeurId(), b.getStatut());
    }

    private TransportDto.ItineraireProfil versProfilItineraire(Itineraire i) {
        return new TransportDto.ItineraireProfil(i.getId(), i.getBusId(), i.getLibelle(), i.getHeureDepartMatin(), i.getHeureRetourSoir());
    }

    private TransportDto.ArretProfil versProfilArret(ArretBus a) {
        return new TransportDto.ArretProfil(a.getId(), a.getItineraireId(), a.getLibelle(),
                a.getLatitude() != null ? a.getLatitude().doubleValue() : null,
                a.getLongitude() != null ? a.getLongitude().doubleValue() : null, a.getOrdre());
    }
}
