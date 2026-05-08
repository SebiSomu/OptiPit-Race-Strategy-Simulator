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

            // ── Circuits ──────────────────────────────────────────────────────
            //
            // softDegScaling guidance (last parameter):
            //   1.0  = neutral (most circuits)
            //   >1.0 = softs degrade faster (blistering, high traction loads)
            //   <1.0 = softs last longer than the generic model expects
            //
            // Historical Pirelli data used as reference for each circuit.
            //
            //                   slug          name                            laps pitLoss trafLoss baseLap  temp   evo
            //                   trac tyreStr aspGrip brak aspAbr lateral trkEvo dwnf  nom         softDeg
            upsertCircuit(circuitRepository,
                    "miami",    "Miami International Autodrome",          57, 20.5,  10.5,  89.708, 50.0, 0.010,
                    3, 3, 4, 3, 2, 3, 5, 3, "C3-C4-C5",   0.82);
            // Miami: 1-stop Medium-Hard is canonical (2023 and 2024 GP).
            // Soft manages well on the well-rubbered Tarmac. 0.82 prevents model
            // from over-degrading C5 and forcing a 2-stop.

            upsertCircuit(circuitRepository,
                    "bahrain",  "Bahrain (Sakhir)",                       57, 22.0,   8.0,  91.447, 42.0, 0.012,
                    4, 3, 3, 4, 5, 3, 4, 3, "C1-C2-C3",   1.10);
            // Bahrain: abrasive desert surface stresses all compounds. C3 soft
            // at Bahrain sees moderate blister risk. 1-stop or 2-stop depending on year.

            upsertCircuit(circuitRepository,
                    "jeddah",   "Jeddah Corniche Circuit",                50, 20.0,   9.0,  90.734, 38.0, 0.008,
                    2, 3, 4, 3, 2, 4, 4, 2, "C2-C3-C4",   1.05);

            upsertCircuit(circuitRepository,
                    "melbourne","Melbourne (Albert Park)",                 58, 21.0,   8.0,  79.813, 34.0, 0.011,
                    2, 3, 2, 2, 2, 3, 4, 3, "C3-C4-C5",   0.95);

            upsertCircuit(circuitRepository,
                    "suzuka",   "Suzuka Circuit",                         53, 22.0,   7.0,  90.965, 32.0, 0.014,
                    3, 5, 4, 2, 4, 5, 3, 4, "C1-C2-C3",   1.40);
            // Suzuka: high-g 130R corner and Degner heavily stress rear tyres.
            // Historically C1 (soft at Suzuka) blisters if pushed too hard.
            // 2-stop is common; 1.40 pushes soft stint lengths shorter.

            upsertCircuit(circuitRepository,
                    "shanghai", "Shanghai International Circuit",         56, 23.0,   8.0,  92.238, 35.0, 0.010,
                    3, 4, 3, 4, 3, 4, 5, 3, "C2-C3-C4",   1.05);

            upsertCircuit(circuitRepository,
                    "monaco",   "Monaco (Monte Carlo)",                   78, 25.0,  14.0,  72.909, 30.0, 0.005,
                    5, 1, 2, 2, 1, 1, 5, 5, "C3-C4-C5",   0.75);
            // Monaco: very slow corners, short straights — softs barely degrade.
            // Race is effectively a tyre management exercise; 2-stop (C5-C4-C3)
            // is historical norm purely to cover undercut threat in the queue.

            upsertCircuit(circuitRepository,
                    "montreal", "Montreal (Gilles Villeneuve)",           70, 18.0,   9.0,  73.078, 36.0, 0.009,
                    4, 3, 2, 5, 1, 1, 5, 1, "C3-C4-C5",   0.90);
            // Montreal: heavy braking but smooth tarmac. Soft manages 20-25 laps.

            upsertCircuit(circuitRepository,
                    "barcelona","Barcelona (Catalunya)",                  66, 22.0,   7.0,  75.743, 45.0, 0.013,
                    3, 5, 3, 3, 4, 5, 3, 4, "C1-C2-C3",   1.25);
            // Barcelona: highest tyre stress of the calendar. tyreStress=5 already
            // pushes toward 2-stop; 1.25 ensures C3 (soft here) doesn't last 30+ laps.

            upsertCircuit(circuitRepository,
                    "spielberg","Red Bull Ring (Spielberg)",              71, 20.0,   6.0,  67.924, 40.0, 0.010,
                    3, 4, 3, 4, 4, 3, 3, 2, "C3-C4-C5",   1.05);

            upsertCircuit(circuitRepository,
                    "silverstone","Silverstone Circuit",                  52, 20.0,   6.0,  87.097, 28.0, 0.015,
                    3, 5, 4, 2, 2, 5, 2, 4, "C2-C3-C4",   1.35);
            // Silverstone: Maggotts-Becketts-Chapel complex imposes huge lateral loads.
            // Soft blistering is common historically. 2-stop is canonical.

            upsertCircuit(circuitRepository,
                    "budapest", "Hungaroring (Budapest)",                 70, 20.0,  11.0,  76.627, 52.0, 0.012,
                    3, 5, 4, 3, 3, 5, 4, 4, "C3-C4-C5",   1.10);
            // Budapest: hot + high lateral. 2-stop frequent.

            upsertCircuit(circuitRepository,
                    "spa",      "Spa-Francorchamps",                      44, 21.0,   6.0, 104.701, 25.0, 0.011,
                    4, 5, 4, 5, 2, 5, 4, 4, "C2-C3-C4",   1.15);

            upsertCircuit(circuitRepository,
                    "zandvoort","Zandvoort (Circuit Park)",                72, 19.0,  11.0,  71.097, 32.0, 0.010,
                    3, 5, 3, 3, 3, 4, 4, 4, "C2-C3-C4",   1.20);
            // Zandvoort: banked corners. Blister risk on soft in heat.

            upsertCircuit(circuitRepository,
                    "monza",    "Monza (Autodromo Nazionale)",            53, 23.0,   5.0,  80.901, 48.0, 0.009,
                    3, 3, 3, 4, 2, 2, 4, 1, "C3-C4-C5",   0.90);
            // Monza: mainly traction and braking, minimal lateral. Soft lasts well.

            upsertCircuit(circuitRepository,
                    "baku",     "Baku City Circuit",                      51, 21.0,  13.0, 103.009, 44.0, 0.008,
                    5, 3, 2, 4, 1, 1, 5, 2, "C3-C4-C5",   1.85);
            // Baku: blistering on softs at T16 traction zone is well-documented.
            // 1.85 makes C5 (soft) degrade fast enough that Medium-Hard 1-stop wins,
            // matching Pirelli recommendations every year.

            upsertCircuit(circuitRepository,
                    "singapore","Singapore (Marina Bay)",                 62, 28.0,  14.0,  93.808, 32.0, 0.007,
                    4, 2, 3, 5, 3, 2, 5, 5, "C3-C4-C5",   0.88);
            // Singapore: night race, cooler temps, low-speed. Soft manages well.
            // Pit cost = 42s → heavily penalises multi-stop. 1-stop wins historically.

            upsertCircuit(circuitRepository,
                    "austin",   "Austin (COTA)",                          56, 20.0,   8.0,  96.169, 46.0, 0.012,
                    3, 4, 4, 4, 4, 4, 4, 4, "C1-C3-C4",   1.15);

            upsertCircuit(circuitRepository,
                    "mexico",   "Mexico City (Hermanos Rodriguez)",       71, 21.0,   8.0,  77.774, 42.0, 0.010,
                    3, 2, 2, 3, 2, 2, 5, 5, "C2-C4-C5",   0.85);
            // Mexico: high altitude reduces aerodynamic downforce and aero-induced
            // wear. Softs last much longer than at sea-level circuits.

            upsertCircuit(circuitRepository,
                    "interlagos","Interlagos (Sao Paulo)",                71, 18.0,   9.0,  70.540, 38.0, 0.011,
                    3, 3, 3, 3, 2, 3, 5, 4, "C2-C3-C4",   1.00);

            upsertCircuit(circuitRepository,
                    "las-vegas","Las Vegas Strip Circuit",                 50, 20.0,   8.0,  93.365, 18.0, 0.006,
                    2, 4, 1, 3, 2, 2, 5, 1, "C3-C4-C5",   1.55);
            // Las Vegas: night race on cold, abrasive tarmac. Extreme thermal
            // stress for softs (heat cycles on/off). 2-stop was optimal in 2023.

            upsertCircuit(circuitRepository,
                    "lusail",   "Lusail International Circuit",           57, 23.0,   8.0,  82.384, 44.0, 0.013,
                    3, 5, 4, 3, 2, 5, 4, 4, "C1-C2-C3",   1.30);
            // Lusail: high rear traction demands. C3 (soft here) blisters historically.

            upsertCircuit(circuitRepository,
                    "yas-marina","Yas Marina (Abu Dhabi)",                58, 22.0,   7.0,  85.637, 35.0, 0.010,
                    4, 3, 3, 4, 3, 3, 4, 3, "C3-C4-C5",   0.95);

            // ── Tyre Compounds ────────────────────────────────────────────────
            //
            // FIX: Recalibrated degradation coefficients.
            // Previous values (C4=0.040, C5=0.058) were too high — the cliff model
            // amplifies them significantly, making long stints on soft/medium
            // unrealistically expensive and biasing the optimizer toward 2-stops.
            //
            // New values derived by back-calculating from known Pirelli stint lengths:
            //   C5 soft ~22-26 laps at a neutral circuit (tyreStress=3, asphaltAbrasion=3)
            //   C4 medium ~28-35 laps at same conditions
            //   C3 hard  ~35-45 laps
            //
            // degradationCoefficient = base deg per lap at 35°C (NOMINAL_TEMP)
            // initialGrip = kept as-is; StrategyOptimizer maps this to relative pace [0.0, 0.004, 0.009]
            // tempSensitivity = extra deg per 10°C above nominal
            // wetPerformance = 0.0 (slick), 1.0 (full wet)
            //
            upsertCompound(tyreCompoundRepository, "C1",           0.010, -0.015, 0.003, 0.0);
            upsertCompound(tyreCompoundRepository, "C2",           0.014, -0.007, 0.005, 0.0);
            upsertCompound(tyreCompoundRepository, "C3",           0.024,  0.000, 0.007, 0.0);
            upsertCompound(tyreCompoundRepository, "C4",           0.036,  0.007, 0.010, 0.0);
            upsertCompound(tyreCompoundRepository, "C5",           0.048,  0.015, 0.013, 0.0);
            upsertCompound(tyreCompoundRepository, "Intermediate", 0.035, -0.020, 0.004, 0.70);
            upsertCompound(tyreCompoundRepository, "Wet",          0.025, -0.040, 0.002, 1.00);
        };
    }

    private void upsertCircuit(CircuitRepository repo,
                               String slug, String name, int laps, double pitLoss, double trafficLoss,
                               double baseLap, double temp, double evo,
                               int traction, int tyreStress, int asphaltGrip, int braking,
                               int asphaltAbrasion, int lateral, int trackEvolution, int downforce,
                               String tyreNomination, double softDegScaling) {
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
                    c.setTyreNomination(tyreNomination);
                    c.setSoftDegScaling(softDegScaling);
                    repo.save(c);
                },
                () -> {
                    Circuit circuit = new Circuit();
                    circuit.setName(name);
                    circuit.setSlug(slug);
                    circuit.setLaps(laps);
                    circuit.setPitStopLoss(pitLoss);
                    circuit.setTrafficLoss(trafficLoss);
                    circuit.setBaseLapTime(baseLap);
                    circuit.setTrackTempNominal(temp);
                    circuit.setTrackEvolutionPerLap(evo);
                    circuit.setTraction(traction);
                    circuit.setTyreStress(tyreStress);
                    circuit.setAsphaltGrip(asphaltGrip);
                    circuit.setBraking(braking);
                    circuit.setAsphaltAbrasion(asphaltAbrasion);
                    circuit.setLateralForces(lateral);
                    circuit.setTrackEvolution(trackEvolution);
                    circuit.setDownforce(downforce);
                    circuit.setTyreNomination(tyreNomination);
                    circuit.setSoftDegScaling(softDegScaling);
                    repo.save(circuit);
                });
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
                () -> repo.save(new TyreCompound(null, name, baseDeg, grip, tempSens, wetPerf)));
    }
}