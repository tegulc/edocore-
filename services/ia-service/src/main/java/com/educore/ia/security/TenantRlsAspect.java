package com.educore.ia.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("!test")
public class TenantRlsAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public void positionnerContexteTenant() {
        Long etablissementId = TenantContext.courant();
        if (etablissementId == null) return;
        entityManager.createNativeQuery("SELECT set_config('app.etablissement_id', :valeur, true)")
                .setParameter("valeur", String.valueOf(etablissementId))
                .getSingleResult();
    }
}
