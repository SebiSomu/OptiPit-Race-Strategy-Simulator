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
    CommandLineRunner initData(CircuitRepository circuitRepository,
                               TyreCompoundRepository tyreCompoundRepository) {
        return args -> {
            upsertCircuit(circuitRepository, "miami", "Miami International Autodrome", 57, 20.5, 10.5, 89.708, 50.0, 0.010,
                    3, 3, 4, 3, 2, 3, 5, 3);
            upsertCircuit(circuitRepository, "bahrain", "Bahrain (Sakhir)", 57, 22.0, 8.0, 91.447, 42.0, 0.012,
                    4, 3, 3, 4, 5, 3, 4, 3);
            upsertCircuit(circuitRepository, "jeddah", "Jeddah Corniche Circuit", 50, 20.0, 9.0, 90.734, 38.0, 0.008,
                    2, 3, 4, 3, 2, 4, 4, 2);
            upsertCircuit(circuitRepository, "melbourne", "Melbourne (Albert Park)", 58, 21.0, 8.0, 79.813, 34.0, 0.011,
                    2, 3, 2, 2, 2, 3, 4, 3);
            upsertCircuit(circuitRepository, "suzuka", "Suzuka Circuit", 53, 22.0, 7.0, 90.965, 32.0, 0.014,
                    3, 5, 4, 2, 4, 5, 3, 4);
            upsertCircuit(circuitRepository, "shanghai", "Shanghai International Circuit", 56, 23.0, 8.0, 92.238, 35.0, 0.010,
                    3, 4, 3, 4, 3, 4, 5, 3);
            upsertCircuit(circuitRepository, "monaco", "Monaco (Monte Carlo)", 78, 25.0, 14.0, 72.909, 30.0, 0.005,
                    5, 1, 2, 2, 1, 1, 5, 5);
            upsertCircuit(circuitRepository, "montreal", "Montreal (Gilles Villeneuve)", 70, 18.0, 9.0, 73.078, 36.0, 0.009,
                    4, 3, 2, 5, 1, 1, 5, 1);
            upsertCircuit(circuitRepository, "barcelona", "Barcelona (Catalunya)", 66, 22.0, 7.0, 75.743, 45.0, 0.013,
                    3, 5, 3, 3, 4, 5, 3, 4);
            upsertCircuit(circuitRepository, "spielberg", "Red Bull Ring (Spielberg)", 71, 20.0, 6.0, 67.924, 40.0, 0.010,
                    3, 4, 3, 4, 4, 3, 3, 2);
            upsertCircuit(circuitRepository, "silverstone", "Silverstone Circuit", 52, 20.0, 6.0, 87.097, 28.0, 0.015,
                    3, 5, 4, 2, 2, 5, 2, 4);
            upsertCircuit(circuitRepository, "budapest", "Hungaroring (Budapest)", 70, 20.0, 11.0, 76.627, 52.0, 0.012,
                    3, 5, 4, 3, 3, 5, 4, 4);
            upsertCircuit(circuitRepository, "spa", "Spa-Francorchamps", 44, 21.0, 6.0, 104.701, 25.0, 0.011,
                    4, 5, 4, 5, 2, 5, 4, 4);
            upsertCircuit(circuitRepository, "zandvoort", "Zandvoort (Circuit Park)", 72, 19.0, 11.0, 71.097, 32.0, 0.010,
                    3, 5, 3, 3, 3, 4, 4, 4);
            upsertCircuit(circuitRepository, "monza", "Monza (Autodromo Nazionale)", 53, 23.0, 5.0, 80.901, 48.0, 0.009,
                    3, 3, 3, 4, 2, 2, 4, 1);
            upsertCircuit(circuitRepository, "baku", "Baku City Circuit", 51, 21.0, 13.0, 103.009, 44.0, 0.008,
                    5, 3, 2, 4, 1, 1, 5, 2);
            upsertCircuit(circuitRepository, "singapore", "Singapore (Marina Bay)", 62, 28.0, 14.0, 93.808, 32.0, 0.007,
                    4, 2, 3, 5, 3, 2, 5, 5);
            upsertCircuit(circuitRepository, "austin", "Austin (COTA)", 56, 20.0, 8.0, 96.169, 46.0, 0.012,
                    3, 4, 4, 4, 4, 4, 4, 4);
            upsertCircuit(circuitRepository, "mexico", "Mexico City (Hermanos Rodriguez)", 71, 21.0, 8.0, 77.774, 42.0, 0.010,
                    3, 2, 2, 3, 2, 2, 5, 5);
            upsertCircuit(circuitRepository, "interlagos", "Interlagos (Sao Paulo)", 71, 18.0, 9.0, 70.540, 38.0, 0.011,
                    3, 3, 3, 3, 2, 3, 5, 4);
            upsertCircuit(circuitRepository, "las-vegas", "Las Vegas Strip Circuit", 50, 20.0, 8.0, 93.365, 18.0, 0.006,
                    2, 4, 1, 3, 2, 2, 5, 1);
            upsertCircuit(circuitRepository, "lusail", "Lusail International Circuit", 57, 23.0, 8.0, 82.384, 44.0, 0.013,
                    3, 5, 4, 3, 2, 5, 4, 4);
            upsertCircuit(circuitRepository, "yas-marina", "Yas Marina (Abu Dhabi)", 58, 22.0, 7.0, 85.637, 35.0, 0.010,
                    4, 3, 3, 4, 3, 3, 4, 3);

            upsertCompound(tyreCompoundRepository, "Soft",         0.095,  0.50,  0.015,    0.0);
            upsertCompound(tyreCompoundRepository, "Medium",       0.045,  0.00,  0.009,    0.0);
            upsertCompound(tyreCompoundRepository, "Hard",         0.022, -0.50,  0.005,    0.0);
            upsertCompound(tyreCompoundRepository, "Intermediate", 0.035, -1.50,  0.004,    0.7);
            upsertCompound(tyreCompoundRepository, "Wet",          0.025, -3.50,  0.002,    1.0);
        };
    }

    private void upsertCircuit(CircuitRepository repo, String slug, String name, int laps, double pitLoss,
                               double trafficLoss, double baseLap, double temp, double evo,
                               int traction, int tyreStress, int asphaltGrip, int braking,
                               int asphaltAbrasion, int lateral, int trackEvolution, int downforce) {
        repo.findByName(name).ifPresentOrElse(
                c -> {
                    c.setSlug(slug);
                    c.setLaps(laps);
                    c.setPitStopLoss(pitLoss);
                    c.setTrafficLoss(trafficLoss);
                    c.setBaseLapTime(baseLap);
                    c.setTrackTempNominal(temp);
                    c.setTrackEvolutionPerLap(evo);
                    c.setTraction(traction);
                    c.setTyreStress(tyreStress);
                    c.setAsphaltGrip(asphaltGrip);
                    c.setBraking(braking);
                    c.setAsphaltAbrasion(asphaltAbrasion);
                    c.setLateralForces(lateral);
                    c.setTrackEvolution(trackEvolution);
                    c.setDownforce(downforce);
                    repo.save(c);
                },
                () -> repo.save(new Circuit(null, name, slug, laps, pitLoss, trafficLoss, baseLap, temp, evo,
                        traction, tyreStress, asphaltGrip, braking, asphaltAbrasion, lateral, trackEvolution, downforce))
        );
    }

    private void upsertCompound(TyreCompoundRepository repo, String name,
                                double baseDeg, double grip, double tempSens, double wetPerf) {
        repo.findByName(name).ifPresentOrElse(
                c -> {
                    c.setDegradationCoefficient(baseDeg);
                    c.setInitialGrip(grip);
                    c.setTempSensitivity(tempSens);
                    c.setWetPerformance(wetPerf);
                    repo.save(c);
                },
                () -> repo.save(new TyreCompound(null, name, baseDeg, grip, tempSens, wetPerf))
        );
    }
}
