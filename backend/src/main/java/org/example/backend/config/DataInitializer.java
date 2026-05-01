package org.example.backend.config;

import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.example.backend.repository.CircuitRepository;
import org.example.backend.repository.TyreCompoundRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CircuitRepository circuitRepository,
                               TyreCompoundRepository tyreCompoundRepository) {
        return args -> {

            // ── Circuits 2026 Calendar ──────────────────────────────────────
            upsertCircuit(circuitRepository, "miami", "Miami International Autodrome", 57, 21.0, 92.0, 50.0, 0.010);
            upsertCircuit(circuitRepository, "bahrain", "Bahrain (Sakhir)", 57, 22.0, 93.0, 42.0, 0.012);
            upsertCircuit(circuitRepository, "jeddah", "Jeddah Corniche Circuit", 50, 20.0, 90.0, 38.0, 0.008);
            upsertCircuit(circuitRepository, "melbourne", "Melbourne (Albert Park)", 58, 20.0, 78.0, 34.0, 0.011);
            upsertCircuit(circuitRepository, "suzuka", "Suzuka Circuit", 53, 22.0, 91.0, 32.0, 0.014);
            upsertCircuit(circuitRepository, "shanghai", "Shanghai International Circuit", 56, 23.0, 95.0, 35.0, 0.010);
            upsertCircuit(circuitRepository, "monaco", "Monaco (Monte Carlo)", 78, 25.0, 74.0, 30.0, 0.005);
            upsertCircuit(circuitRepository, "montreal", "Montreal (Gilles Villeneuve)", 70, 18.0, 73.0, 36.0, 0.009);
            upsertCircuit(circuitRepository, "barcelona", "Barcelona (Catalunya)", 66, 22.0, 76.0, 45.0, 0.013);
            upsertCircuit(circuitRepository, "spielberg", "Red Bull Ring (Spielberg)", 71, 20.0, 68.0, 40.0, 0.010);
            upsertCircuit(circuitRepository, "silverstone", "Silverstone Circuit", 52, 20.0, 89.0, 28.0, 0.015);
            upsertCircuit(circuitRepository, "budapest", "Hungaroring (Budapest)", 70, 20.0, 79.0, 52.0, 0.012);
            upsertCircuit(circuitRepository, "spa", "Spa-Francorchamps", 44, 21.0, 105.0, 25.0, 0.011);
            upsertCircuit(circuitRepository, "zandvoort", "Zandvoort (Circuit Park)", 72, 19.0, 72.0, 32.0, 0.010);
            upsertCircuit(circuitRepository, "monza", "Monza (Autodromo Nazionale)", 53, 23.0, 81.0, 48.0, 0.009);
            upsertCircuit(circuitRepository, "baku", "Baku City Circuit", 51, 21.0, 103.0, 44.0, 0.008);
            upsertCircuit(circuitRepository, "singapore", "Singapore (Marina Bay)", 62, 28.0, 100.0, 32.0, 0.007);
            upsertCircuit(circuitRepository, "austin", "Austin (COTA)", 56, 20.0, 98.0, 46.0, 0.012);
            upsertCircuit(circuitRepository, "mexico", "Mexico City (Hermanos Rodriguez)", 71, 21.0, 79.0, 42.0, 0.010);
            upsertCircuit(circuitRepository, "interlagos", "Interlagos (Sao Paulo)", 71, 18.0, 70.0, 38.0, 0.011);
            upsertCircuit(circuitRepository, "las-vegas", "Las Vegas Strip Circuit", 50, 20.0, 93.0, 18.0, 0.006);
            upsertCircuit(circuitRepository, "lusail", "Lusail International Circuit", 57, 23.0, 84.0, 44.0, 0.013);
            upsertCircuit(circuitRepository, "yas-marina", "Yas Marina (Abu Dhabi)", 58, 22.0, 86.0, 35.0, 0.010);

            // ── Tyre Compounds ──────────────────
            upsertCompound(tyreCompoundRepository, "Soft", 0.075, 0.50, 0.015);
            upsertCompound(tyreCompoundRepository, "Medium", 0.045, 0.00, 0.009);
            upsertCompound(tyreCompoundRepository, "Hard", 0.022, -0.50, 0.005);
        };
    }

    private void upsertCircuit(CircuitRepository repo, String slug, String name, int laps, double pitLoss, 
                               double baseLap, double temp, double evo) {
        repo.findByName(name).ifPresentOrElse(
                c -> {
                    c.setSlug(slug);
                    c.setLaps(laps);
                    c.setPitStopLoss(pitLoss);
                    c.setBaseLapTime(baseLap);
                    c.setTrackTempNominal(temp);
                    c.setTrackEvolutionPerLap(evo);
                    repo.save(c);
                },
                () -> repo.save(new Circuit(null, name, slug, laps, pitLoss, baseLap, temp, evo))
        );
    }

    private void upsertCompound(TyreCompoundRepository repo, String name, double baseDeg, double grip, double tempSens) {
        repo.findByName(name).ifPresentOrElse(
                c -> {
                    c.setDegradationCoefficient(baseDeg);
                    c.setInitialGrip(grip);
                    c.setTempSensitivity(tempSens);
                    repo.save(c);
                },
                () -> repo.save(new TyreCompound(null, name, baseDeg, grip, tempSens))
        );
    }
}
