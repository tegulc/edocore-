package com.educore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EDUCORE / SIGES - Systeme ERP Intelligent de Gestion d'Etablissements Scolaires.
 * Point d'entree de l'application backend (architecture MVC + REST).
 *
 * Phase 0 : socle technique. Les modules metier (auth, etablissements, eleves, ...)
 * sont ajoutes progressivement sous com.educore.modules.*
 */
@SpringBootApplication
@EnableScheduling // pour les relances automatiques de paiement, taches de nettoyage, etc.
@EnableAsync       // pour l'envoi de notifications (SMS/email/push) sans bloquer les requetes
public class EducoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducoreApplication.class, args);
    }
}
