package com.educore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Force l'advisor transactionnel a l'ordre le plus "exterieur" possible
 * (HIGHEST_PRECEDENCE). Necessaire pour garantir que TenantRlsAspect
 * (security package, ordre par defaut LOWEST_PRECEDENCE) s'execute toujours
 * A L'INTERIEUR de la transaction Spring deja ouverte, jamais avant : sans
 * cette configuration explicite, l'ordre relatif entre deux advisors tous
 * deux a l'ordre par defaut n'est pas garanti par Spring, ce qui rendrait
 * la securisation Row-Level Security (set_config par transaction) non fiable.
 */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class TransactionManagementConfig {
}
