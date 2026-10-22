package com.educore.modules.classe.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Getter @Setter
@Entity
@Table(name = "etablissement_langue_option",
       uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "matiere_id", "type_offre"}))
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class EtablissementLangueOption implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "matiere_id", nullable = false) private Long matiereId;
    @Column(name = "type_offre", nullable = false, length = 20) private String typeOffre; // LANGUE_VIVANTE, OPTION
    @Column(name = "niveau_min_id") private Long niveauMinId; // NULL si disponible dès la 6e
}
