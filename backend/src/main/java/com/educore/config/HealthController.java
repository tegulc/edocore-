package com.educore.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint de sante simple, complementaire a /actuator/health,
 * pratique pour verifier rapidement que le socle Phase 0 tourne.
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/status")
    public Map<String, Object> statut() {
        return Map.of(
                "service", "educore-backend",
                "phase", "Phase 0 - socle technique",
                "horodatage", Instant.now().toString(),
                "statut", "OK"
        );
    }
}
