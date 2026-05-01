package org.example.backend.config;

import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.example.backend.repository.CircuitRepository;
import org.example.backend.repository.TyreCompoundRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds/updates all master data on startup.
 *
 * === TYRE COMPOUND DATA (Pirelli 2024 realistic base values at 35°C standard) ===
 *
 *  Soft (C4):
 *    paceAdvantage    = +0.30s vs Medium (fastest in qualifying trim)
 *    baseDeg          = 0.040 s/lap at 35°C
 *    tempSensitivity  = 0.010 s/lap per 10°C  → very sensitive to heat
 *    Miami (50°C) effective deg: 0.040 + 0.015 = 0.055 s/lap
 *
 *  Medium (C3):
 *    paceAdvantage    = 0.00s (baseline)
 *    baseDeg          = 0.025 s/lap at 35°C
 *    tempSensitivity  = 0.007 s/lap per 10°C
 *    Miami effective deg: 0.025 + 0.0105 = 0.036 s/lap
 *
 *  Hard (C2):
 *    paceAdvantage    = −0.40s vs Medium (slower initially)
 *    baseDeg          = 0.011 s/lap at 35°C  → extremely durable
 *    tempSensitivity  = 0.004 s/lap per 10°C
 *    Miami effective deg: 0.011 + 0.006 = 0.017 s/lap
 *
 * === CIRCUIT: Miami International Autodrome ===
 *  Laps: 57, Pit stop loss: 21s, Base lap time: 92s
 *  Track temp nominal: 50°C (May in Miami, very hot surface)
 *  Track evolution: 0.010 s/lap (smooth asphalt rubbers in steadily)
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CircuitRepository circuitRepository,
                               TyreCompoundRepository tyreCompoundRepository) {
        return args -> {

            // ── Circuit ──────────────────────────────────────────────────────
            upsertCircuit(circuitRepository,
                    "Miami International Autodrome",
                    57,       // race laps
                    21.0,     // pit stop time loss (s)
                    92.0,     // baseline race lap time (s)
                    50.0,     // track temp nominal (°C)
                    0.010     // track evolution per lap (s)
            );

            // ── Tyre Compounds (always sync to latest calibrated values) ─────
            upsertCompound(tyreCompoundRepository,
                    "Soft",
                    0.075,   // baseDeg at 35°C (was 0.040)
                    0.50,    // paceAdvantage vs Medium (was 0.30)
                    0.015);  // tempSensitivity (was 0.010)

            upsertCompound(tyreCompoundRepository,
                    "Medium",
                    0.045,   // baseDeg (was 0.025)
                    0.00,    // paceAdvantage (baseline)
                    0.009);  // tempSensitivity (was 0.007)

            upsertCompound(tyreCompoundRepository,
                    "Hard",
                    0.022,   // baseDeg (was 0.011)
                    -0.50,   // paceAdvantage (was -0.40)
                    0.005);  // tempSensitivity (was 0.004)
        };
    }

    private void upsertCircuit(CircuitRepository repo,
                               String name, int laps, double pitLoss, double baseLap, double temp, double evo) {
        repo.findByName(name).ifPresentOrElse(
                circuit -> {
                    circuit.setLaps(laps);
                    circuit.setPitStopLoss(pitLoss);
                    circuit.setBaseLapTime(baseLap);
                    circuit.setTrackTempNominal(temp);
                    circuit.setTrackEvolutionPerLap(evo);
                    repo.save(circuit);
                },
                () -> repo.save(new Circuit(null, name, laps, pitLoss, baseLap, temp, evo))
        );
    }

    private void upsertCompound(TyreCompoundRepository repo,
                                 String name, double baseDeg, double grip, double tempSens) {
        repo.findByName(name).ifPresentOrElse(
                compound -> {
                    compound.setDegradationCoefficient(baseDeg);
                    compound.setInitialGrip(grip);
                    compound.setTempSensitivity(tempSens);
                    repo.save(compound);
                },
                () -> repo.save(new TyreCompound(null, name, baseDeg, grip, tempSens))
        );
    }
}
