package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "matiere_niveau_serie", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "matiere_id", "niveau_id", "serie_id"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class MatiereNiveauSerie implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "matiere_id", nullable = false) private Long matiereId;
    @Column(name = "niveau_id", nullable = false) private Long niveauId;
    @Column(name = "serie_id") private Long serieId; // NULL si matière commune à toutes les séries
    @Column(nullable = false, precision = 4, scale = 2) private BigDecimal coefficient = BigDecimal.ONE;
    @Column(name = "volume_horaire_hebdo", nullable = false, precision = 4, scale = 2) private BigDecimal volumeHoraireHebdo = BigDecimal.ONE;
    @Column(name = "est_obligatoire", nullable = false) private Boolean estObligatoire = true;
}
