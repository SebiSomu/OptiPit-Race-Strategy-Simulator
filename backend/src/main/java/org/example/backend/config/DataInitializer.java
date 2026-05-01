package org.example.backend.config;

import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.example.backend.repository.CircuitRepository;
import org.example.backend.repository.TyreCompoundRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CircuitRepository circuitRepository, TyreCompoundRepository tyreCompoundRepository) {
        return args -> {
            // Seed Miami International Autodrome
            if (circuitRepository.count() == 0) {
                // Miami International Autodrome: 57 laps, ~21s pit stop loss, ~92s base lap time
                circuitRepository.save(new Circuit(null, "Miami International Autodrome", 57, 21.0, 92.0));
            }

            // Seed tyre compounds with realistic F1 degradation characteristics
            if (tyreCompoundRepository.count() == 0) {
                // Soft: highest grip, fastest degradation
                tyreCompoundRepository.save(new TyreCompound(null, "Soft", 0.045, 1.0));
                // Medium: balanced grip and degradation
                tyreCompoundRepository.save(new TyreCompound(null, "Medium", 0.025, 0.97));
                // Hard: lowest grip, slowest degradation
                tyreCompoundRepository.save(new TyreCompound(null, "Hard", 0.012, 0.94));
            }
        };
    }
}
