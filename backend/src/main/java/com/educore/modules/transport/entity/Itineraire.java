package com.educore.modules.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter @Setter
@Entity
@Table(name = "itineraires")
public class Itineraire {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "bus_id", nullable = false) private Long busId;
    @Column(nullable = false) private String libelle;
    @Column(name = "heure_depart_matin") private LocalTime heureDepartMatin;
    @Column(name = "heure_retour_soir") private LocalTime heureRetourSoir;
}
