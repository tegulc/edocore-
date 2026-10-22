package com.educore.modules.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "arrets_bus")
public class ArretBus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "itineraire_id", nullable = false) private Long itineraireId;
    @Column(nullable = false) private String libelle;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Column(nullable = false) private Short ordre;
}
