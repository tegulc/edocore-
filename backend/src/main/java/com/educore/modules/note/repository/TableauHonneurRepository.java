package com.educore.modules.note.repository;

import com.educore.modules.note.entity.TableauHonneur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableauHonneurRepository extends JpaRepository<TableauHonneur, Long> {
    Optional<TableauHonneur> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<TableauHonneur> findByClasseIdAndPeriodeIdOrderByMoyenneGeneraleDesc(Long classeId, Long periodeId);
    List<TableauHonneur> findByEtablissementIdAndPeriodeIdOrderByMoyenneGeneraleDesc(Long etablissementId, Long periodeId);
}
