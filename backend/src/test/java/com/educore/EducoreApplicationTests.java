package com.educore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumee : verifie que tout le contexte Spring (securite, JPA, JWT,
 * controllers) demarre correctement. Premiere ligne de defense du cycle en V
 * (validation avant chaque livraison de module).
 */
@SpringBootTest
@ActiveProfiles("test")
class EducoreApplicationTests {

    @Test
    void contextLoads() {
        // Si cette methode passe, le socle technique de la Phase 0 est fonctionnel.
    }
}
