package com.educore.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Identique en esprit a educore-backend (Phase 2) : garantit que TenantRlsAspect s'execute dans la transaction deja ouverte. */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class TransactionManagementConfig {
}
