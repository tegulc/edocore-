package com.educore.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Active DEUX lignes de defense independantes pour l'isolation multi-tenant,
 * a chaque transaction (Volume 3, Partie VII) :
 *
 *  1. Le filtre Hibernate "filtreTenant" (@FilterDef declare en package-info.java
 *     de com.educore.common.base, applique via @Filter sur chaque entite
 *     TenantScoped) : restreint des la construction de la requete JPA.
 *  2. La variable de session PostgreSQL app.etablissement_id, consommee par
 *     les politiques Row-Level Security (migration V2) : protege meme une
 *     requete native mal filtree ou un bug applicatif qui contournerait (1).
 *
 * set_config(..., true) et enableFilter sont tous deux lies a la transaction/
 * session courante et ne "fuitent" jamais vers une autre requete malgre le
 * pooling de connexions.
 */
@Aspect
@Component
@Profile("!test") // H2 (profil test) ne supporte pas set_config() ; RLS reste actif partout ou tourne un vrai PostgreSQL
public class TenantRlsAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public void positionnerContexteTenant() {
        Long etablissementId = TenantContext.courant();
        if (etablissementId == null) {
            return; // requetes non authentifiees (ex: /auth/login) : aucun tenant a positionner
        }

        entityManager.unwrap(Session.class)
                .enableFilter("filtreTenant")
                .setParameter("etablissementId", etablissementId);

        entityManager.createNativeQuery("SELECT set_config('app.etablissement_id', :valeur, true)")
                .setParameter("valeur", String.valueOf(etablissementId))
                .getSingleResult();
    }
}
