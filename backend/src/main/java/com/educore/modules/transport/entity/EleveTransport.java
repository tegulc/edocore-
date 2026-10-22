package com.educore.modules.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "eleve_transport", uniqueConstraints = @UniqueConstraint(columnNames = "eleve_id"))
public class EleveTransport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "itineraire_id", nullable = false) private Long itineraireId;
    @Column(name = "arret_id") private Long arretId;
}
