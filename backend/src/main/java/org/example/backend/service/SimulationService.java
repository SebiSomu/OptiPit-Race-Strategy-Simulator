package org.example.backend.service;

import org.springframework.stereotype.Service;

/**
 * General F1 lap time simulation service.
 * Includes Fuel Burn effect, Track Evolution, and Non-linear Tyre Degradation ("The Cliff").
 */
@Service
public class SimulationService {

    private static final double NOMINAL_TEMP = 35.0; 
    private static final double FUEL_BURN_LAPS_EFFECT = 0.060; // s/lap faster as fuel burns

    /**
     * Calculates effective degradation for a compound at given track temperature.
     */
    public double effectiveDegradation(double baseDeg, double tempSensitivity, double trackTemp) {
        double tempDelta = (trackTemp - NOMINAL_TEMP) / 10.0;
        return baseDeg + tempSensitivity * tempDelta;
    }

    /**
     * Calculates the lap time for one lap.
     *
     * @param baseLapTime         circuit baseline lap time (s)
     * @param baseDeg             compound base degradation at 35°C (s/lap)
     * @param tempSensitivity     compound extra deg per 10°C above nominal (s/lap)
     * @param paceAdvantage       compound pace advantage vs Medium baseline (s, positive=faster)
     * @param lapOnTyre           lap number on THIS tyre set (1, 2, 3…)
     * @param globalLap           actual race lap number (1…totalLaps)
     * @param trackTemp           current track surface temperature (°C)
     * @param trackEvolution      track improvement per lap from rubber (s/lap)
     */
    public double calculateLapTime(double baseLapTime,
                                    double baseDeg, double tempSensitivity, double paceAdvantage,
                                    int lapOnTyre, int globalLap,
                                    double trackTemp, double trackEvolution) {
        
        double degBase = effectiveDegradation(baseDeg, tempSensitivity, trackTemp);
        
        // Non-linear degradation (the "cliff"): degradation accelerates as the tyre wears out.
        // Power of 1.8 ensures that after 20-30 laps, the tyre starts losing seconds, not just decimals.
        double wearEffect = (degBase * lapOnTyre) + (degBase * 0.02 * Math.pow(lapOnTyre, 1.8));
        
        // Track evolution: track gets faster
        double trackBonus = trackEvolution * (globalLap - 1);
        
        // Fuel effect: car gets faster as fuel is consumed
        double fuelBonus = FUEL_BURN_LAPS_EFFECT * (globalLap - 1);
        
        return baseLapTime - paceAdvantage + wearEffect - trackBonus - fuelBonus;
    }

    /**
     * Calculates total stint time.
     */
    public double calculateStintTime(double baseLapTime,
                                      double baseDeg, double tempSensitivity, double paceAdvantage,
                                      int stintLaps, int startGlobalLap,
                                      double trackTemp, double trackEvolution) {
        double total = 0;
        for (int lapOnTyre = 1; lapOnTyre <= stintLaps; lapOnTyre++) {
            int globalLap = startGlobalLap + lapOnTyre - 1;
            total += calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                    lapOnTyre, globalLap, trackTemp, trackEvolution);
        }
        return total;
    }

    // ── Legacy compatibility methods (preserved for safety) ─────
    public double calculateLapTime(double baseLapTime, double degradationCoefficient,
                                    double initialGrip, int lapOnTyre) {
        return baseLapTime - initialGrip + (degradationCoefficient * lapOnTyre);
    }

    public double calculateStintTime(double baseLapTime, double degradationCoefficient,
                                      double initialGrip, int lapsInStint) {
        double total = 0;
        for (int i = 1; i <= lapsInStint; i++) {
            total += calculateLapTime(baseLapTime, degradationCoefficient, initialGrip, i);
        }
        return total;
    }
}
