package com.educore.ia.risque.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "ia_alertes_risque_echec", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "periode_id"}))
public class AlerteRisqueEchec {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "periode_id", nullable = false) private Long periodeId;
    @Column(name = "score_risque", nullable = false) private BigDecimal scoreRisque;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facteurs_identifies", columnDefinition = "jsonb") private String facteursIdentifies;
    @Column(name = "genere_le", nullable = false) private Instant genereLe = Instant.now();
}
