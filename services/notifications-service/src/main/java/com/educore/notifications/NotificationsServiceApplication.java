package com.educore.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service independant Chat (Module 22) + Notifications (Module 24), separe
 * du monolithe educore-backend des la V1 (Volume 2 §2.5, Volume 3). Deploye
 * et mis a l'echelle independamment - voir docker-compose.yml (service
 * "notifications", port 8081).
 */
@SpringBootApplication
public class NotificationsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsServiceApplication.class, args);
    }
}
