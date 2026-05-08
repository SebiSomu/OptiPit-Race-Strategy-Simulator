package org.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    private static final double NOMINAL_TEMP = 35.0;

    // Fuel burn constants
    private static final double FUEL_EFFECT_PER_KG = 0.03;
    private static final double FUEL_CONSUMPTION_PER_LAP = 1.75;
    private static final double MAX_FUEL_CAPACITY = 110.0;

    // ── Track evolution constants ──────────────────────────────────────
    private static final double TRACK_EVOLUTION_SATURATION_RATE = 0.35;
    private static final double MAX_TRACK_EVOLUTION_BONUS = 0.15;

    // ── Tyre surface temperature constants ───────────────────────────────
    private static final double TYRE_TEMP_OPTIMAL = 90.0;
    private static final double TYRE_TEMP_BASE = 60.0;
    private static final double TYRE_TEMP_PUSH_FACTOR = 8.0;
    private static final double TYRE_TEMP_NORMAL_FACTOR = 3.0;
    private static final double TYRE_TEMP_CONSERVATIVE_FACTOR = 1.0;
    private static final double TYRE_TEMP_COOLING = 2.0;
    private static final double TYRE_DEG_TEMP_MULTIPLIER = 0.02;

    // ── Wind constants ───────────────────────────────────────────────────
    private static final double WIND_DRAG_COEFFICIENT = 0.015;

    // ── Air temperature constants ────────────────────────────────────────
    private static final double AIR_TEMP_REFERENCE = 25.0;
    private static final double AIR_TEMP_SENSITIVITY = 0.025;

    // ── Rain constants ───────────────────────────────────────────────────
    private static final double RAIN_PENALTY_BASE = 4.0;
    private static final double RAIN_GRIP_MULTIPLIER = 1.5;

    // ── Safety Car / VSC constants ─────────────────────────────────────────
    private static final double SAFETY_CAR_PACE_PENALTY = 25.0;
    private static final double VSC_PACE_PENALTY = 12.0;
    private static final double SAFETY_CAR_PROBABILITY_PER_LAP = 0.02;
    private static final double VSC_PROBABILITY_PER_LAP = 0.03;

    // ── Tyre warmup constants ────────────────────────────────────────────
    private static final double WARMUP_BASE_PENALTY = 0.5;
    private static final double WARMUP_SENSITIVITY = 20.0;

    // ── Short stint penalty constants ────────────────────────────────────
    // FIX: thresholds recalibrated to match actual compound baseDeg values:
    //   C5 (soft) = 0.048 → full penalty applies when baseDeg > 0.044
    //   C4 (medium) = 0.036 → half penalty applies when baseDeg > 0.028
    //   C3 (hard)  = 0.024 → no penalty
    // Previously: 0.07 / 0.035 — C5 at 0.058 never exceeded 0.07 so soft
    // never got its full penalty, making short soft stints artificially cheap.
    private static final int OPTIMAL_STINT_LENGTH = 12;
    private static final double SHORT_STINT_PENALTY_RATE = 0.05;
    private static final double SOFT_DEG_THRESHOLD = 0.044;   // > this = soft compound
    private static final double MEDIUM_DEG_THRESHOLD = 0.028; // > this = medium compound

    private static final double CLIFF_SATURATION_LAP = 22.0;

    private static final ThreadLocal<double[]> CIRCUIT_CONTEXT = new ThreadLocal<>();

    // ── Out-lap penalty ──────────────────────────────────────────────────
    private static final double OUT_LAP_BASE_PENALTY = 0.8;
    private static final double OUT_LAP_SOFT_FACTOR = 1.125;
    private static final double OUT_LAP_MEDIUM_FACTOR = 0.875;
    private static final double OUT_LAP_HARD_FACTOR = 0.625;

    // ── Driving modes ────────────────────────────────────────────────────
    public enum DrivingMode {
        CONSERVATIVE(0.5, 1.0),
        NORMAL(1.0, 1.0),
        PUSH(1.02, 1.8);

        final double paceMultiplier;
        final double degMultiplier;

        DrivingMode(double pace, double deg) {
            this.paceMultiplier = pace;
            this.degMultiplier = deg;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Degradation
    // ─────────────────────────────────────────────────────────────────────

    public double effectiveDegradation(double baseDeg, double tempSensitivity,
                                       double trackTemp, double rainIntensity,
                                       double wetPerformance, DrivingMode drivingMode) {
        double tempDelta = (trackTemp - NOMINAL_TEMP) / 10.0;
        double baseDegAtTemp = baseDeg + tempSensitivity * tempDelta;
        double rainEffect = rainIntensity * RAIN_GRIP_MULTIPLIER * (1.0 - wetPerformance);
        double rainMultiplier = 1.0 + rainEffect;
        return baseDegAtTemp * rainMultiplier;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Core lap time calculation
    // ─────────────────────────────────────────────────────────────────────

    public double calculateLapTime(double baseLapTime,
                                   double baseDeg, double tempSensitivity, double paceAdvantage,
                                   int lapOnTyre, int globalLap,
                                   double trackTemp, double trackEvolution,
                                   double windSpeed, double windAngle,
                                   double airTemp, double rainIntensity,
                                   double wetPerformance, DrivingMode drivingMode, int totalRaceLaps) {

        // ── Tyre surface temperature ──
        double tempIncreasePerLap;
        switch (drivingMode) {
            case PUSH -> tempIncreasePerLap = TYRE_TEMP_PUSH_FACTOR;
            case CONSERVATIVE -> tempIncreasePerLap = TYRE_TEMP_CONSERVATIVE_FACTOR;
            default -> tempIncreasePerLap = TYRE_TEMP_NORMAL_FACTOR;
        }
        double dynamicCooling = TYRE_TEMP_COOLING + (AIR_TEMP_REFERENCE - airTemp) * 0.1;
        double currentTyreTemp = TYRE_TEMP_BASE + (tempIncreasePerLap - dynamicCooling) * lapOnTyre;
        currentTyreTemp = Math.min(currentTyreTemp, 120.0);

        // ── Tyre degradation (non-linear cliff with temperature factor) ──
        double degBase = effectiveDegradation(baseDeg, tempSensitivity, trackTemp, rainIntensity,
                wetPerformance, drivingMode);

        double tempAboveOptimal = Math.max(0, currentTyreTemp - TYRE_TEMP_OPTIMAL);
        double tempDegMultiplier = 1.0 + (TYRE_DEG_TEMP_MULTIPLIER * tempAboveOptimal * drivingMode.degMultiplier);

        double circuitDegMultiplier = getCircuitCharacteristicMultiplier();

        double wearEffect;
        if (lapOnTyre <= 2) {
            wearEffect = degBase * lapOnTyre * 0.1 * tempDegMultiplier * circuitDegMultiplier;
        } else if (lapOnTyre <= 6) {
            wearEffect = degBase * lapOnTyre * 0.3 * tempDegMultiplier * circuitDegMultiplier;
        } else {
            int effectiveLap = lapOnTyre - 6;
            double baseWear = degBase * 6 * 0.3;
            double saturatedCliff = Math.pow(effectiveLap, 2.0)
                    / (1.0 + effectiveLap / CLIFF_SATURATION_LAP);
            wearEffect = (baseWear
                    + (degBase * effectiveLap)
                    + (degBase * 0.12 * saturatedCliff))
                    * tempDegMultiplier * circuitDegMultiplier;
        }

        // FIX: Apply circuit-specific soft compound degradation scaling.
        // This captures blistering risk at circuits like Baku, Suzuka, Las Vegas
        // that the smooth cliff model cannot represent.
        // Only applies to the soft compound (highest baseDeg in the weekend set).
        if (baseDeg > SOFT_DEG_THRESHOLD) {
            wearEffect *= getSoftCompoundMultiplier();
        }

        // ── Track evolution ──
        double saturationFactor = 1.0 - Math.exp(-TRACK_EVOLUTION_SATURATION_RATE * globalLap);
        double trackBonus = MAX_TRACK_EVOLUTION_BONUS * saturationFactor;
        if (rainIntensity > 0.3) {
            trackBonus *= (1.0 - rainIntensity * 0.8);
        }

        // ── Fuel effect ──
        double remainingFuel = Math.max(0, MAX_FUEL_CAPACITY - (FUEL_CONSUMPTION_PER_LAP * (globalLap - 1)));
        double fuelPenalty = remainingFuel * FUEL_EFFECT_PER_KG;
        double fuelBonus = (MAX_FUEL_CAPACITY * FUEL_EFFECT_PER_KG) - fuelPenalty;

        // ── Wind effect ──
        double windRadians = Math.toRadians(windAngle);
        double effectiveHeadwind = windSpeed * Math.cos(windRadians);
        double windPenalty = effectiveHeadwind * WIND_DRAG_COEFFICIENT;

        // ── Air temperature effect ──
        double airTempBonus = (AIR_TEMP_REFERENCE - airTemp) * AIR_TEMP_SENSITIVITY;

        // ── Rain effect ──
        double rainPenalty = rainIntensity * RAIN_PENALTY_BASE * (1.0 - wetPerformance * 0.85);
        double dryPenaltyForWets = (1.0 - rainIntensity) * wetPerformance * 2.5;

        // ── Tyre warmup effect ──
        double warmupPenalty = 0.0;
        if (lapOnTyre == 1) {
            warmupPenalty = WARMUP_BASE_PENALTY * (1.0 + baseDeg * WARMUP_SENSITIVITY);
        } else if (lapOnTyre == 2) {
            warmupPenalty = WARMUP_BASE_PENALTY * (1.0 + baseDeg * WARMUP_SENSITIVITY) * 0.4;
        }

        // ── Driving mode pace effect ──
        double drivingModePaceBonus = (drivingMode.paceMultiplier - 1.0) * baseLapTime * -1.0;

        return baseLapTime
                - (baseLapTime * paceAdvantage)
                + wearEffect
                - trackBonus
                - fuelBonus
                + windPenalty
                - airTempBonus
                + rainPenalty
                + dryPenaltyForWets
                + warmupPenalty
                - drivingModePaceBonus;
    }

    /** Backward-compatible overload — defaults to NORMAL driving mode. */
    public double calculateLapTime(double baseLapTime,
                                   double baseDeg, double tempSensitivity, double paceAdvantage,
                                   int lapOnTyre, int globalLap,
                                   double trackTemp, double trackEvolution,
                                   double windSpeed, double windAngle,
                                   double airTemp, double rainIntensity,
                                   double wetPerformance) {
        return calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                lapOnTyre, globalLap, trackTemp, trackEvolution,
                windSpeed, windAngle, airTemp, rainIntensity, wetPerformance,
                DrivingMode.NORMAL, globalLap);
    }

    /** Backward-compatible 8-parameter overload (no wind, 25°C air, dry, NORMAL). */
    public double calculateLapTime(double baseLapTime,
                                   double baseDeg, double tempSensitivity, double paceAdvantage,
                                   int lapOnTyre, int globalLap,
                                   double trackTemp, double trackEvolution) {
        return calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                lapOnTyre, globalLap, trackTemp, trackEvolution,
                0.0, 0.0, 25.0, 0.0, 0.0, DrivingMode.NORMAL, globalLap);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Stint time calculation
    // ─────────────────────────────────────────────────────────────────────

    public double calculateStintTime(double baseLapTime,
                                     double baseDeg, double tempSensitivity, double paceAdvantage,
                                     int stintLaps, int startGlobalLap,
                                     double trackTemp, double trackEvolution,
                                     double windSpeed, double windAngle,
                                     double airTemp, double rainIntensity,
                                     double wetPerformance, DrivingMode drivingMode, int totalRaceLaps) {
        double total = 0;
        for (int lapOnTyre = 1; lapOnTyre <= stintLaps; lapOnTyre++) {
            int globalLap = startGlobalLap + lapOnTyre - 1;
            total += calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                    lapOnTyre, globalLap, trackTemp, trackEvolution,
                    windSpeed, windAngle, airTemp, rainIntensity, wetPerformance,
                    drivingMode, totalRaceLaps);
        }

        // ── Short stint penalty (recalibrated thresholds) ──
        // FIX: Thresholds now match actual compound baseDeg values.
        // See SOFT_DEG_THRESHOLD / MEDIUM_DEG_THRESHOLD constants above.
        double stintLengthPenalty = (stintLaps < OPTIMAL_STINT_LENGTH)
                ? (1.0 + (OPTIMAL_STINT_LENGTH - stintLaps) * SHORT_STINT_PENALTY_RATE)
                : 1.0;
        if (baseDeg > SOFT_DEG_THRESHOLD) {
            // Soft compound — full penalty for short stints
            total *= stintLengthPenalty;
        } else if (baseDeg > MEDIUM_DEG_THRESHOLD) {
            // Medium compound — half penalty
            double mediumPenalty = 1.0 + (stintLengthPenalty - 1.0) * 0.5;
            total *= mediumPenalty;
        }
        // Hard compound — no penalty (doesn't need minimum stint length)

        return total;
    }

    /** Backward-compatible overload — NORMAL driving mode. */
    public double calculateStintTime(double baseLapTime,
                                     double baseDeg, double tempSensitivity, double paceAdvantage,
                                     int stintLaps, int startGlobalLap,
                                     double trackTemp, double trackEvolution,
                                     double windSpeed, double windAngle,
                                     double airTemp, double rainIntensity,
                                     double wetPerformance) {
        return calculateStintTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                stintLaps, startGlobalLap, trackTemp, trackEvolution,
                windSpeed, windAngle, airTemp, rainIntensity, wetPerformance,
                DrivingMode.NORMAL, startGlobalLap + stintLaps - 1);
    }

    /** Backward-compatible 8-parameter overload. */
    public double calculateStintTime(double baseLapTime,
                                     double baseDeg, double tempSensitivity, double paceAdvantage,
                                     int stintLaps, int startGlobalLap,
                                     double trackTemp, double trackEvolution) {
        return calculateStintTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                stintLaps, startGlobalLap, trackTemp, trackEvolution,
                0.0, 0.0, 25.0, 0.0, 0.0, DrivingMode.NORMAL, startGlobalLap + stintLaps - 1);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Safety Car / VSC
    // ─────────────────────────────────────────────────────────────────────

    public enum SafetyCarEvent {
        NONE, VSC, SAFETY_CAR
    }

    public SafetyCarEvent checkSafetyCarEvent(long randomSeed, double rainIntensity, int lapNumber) {
        double firstLapMultiplier = (lapNumber == 1) ? 2.5 : 1.0;
        double rainMultiplier = 1.0 + (rainIntensity * 2.0);
        double effectiveVscProb = VSC_PROBABILITY_PER_LAP * rainMultiplier * firstLapMultiplier;
        double effectiveScProb = SAFETY_CAR_PROBABILITY_PER_LAP * rainMultiplier * firstLapMultiplier;
        java.util.Random random = new java.util.Random(randomSeed + lapNumber);
        double roll = random.nextDouble();
        if (roll < effectiveScProb) return SafetyCarEvent.SAFETY_CAR;
        else if (roll < effectiveScProb + effectiveVscProb) return SafetyCarEvent.VSC;
        return SafetyCarEvent.NONE;
    }

    public double applySafetyCarPenalty(double baseLapTime, SafetyCarEvent event) {
        return switch (event) {
            case SAFETY_CAR -> baseLapTime + SAFETY_CAR_PACE_PENALTY;
            case VSC -> baseLapTime + VSC_PACE_PENALTY;
            case NONE -> baseLapTime;
        };
    }

    public double calculateOutLapPenalty(double baseDeg) {
        double factor;
        if (baseDeg > SOFT_DEG_THRESHOLD) {
            factor = OUT_LAP_SOFT_FACTOR;
        } else if (baseDeg > MEDIUM_DEG_THRESHOLD) {
            factor = OUT_LAP_MEDIUM_FACTOR;
        } else {
            factor = OUT_LAP_HARD_FACTOR;
        }
        return OUT_LAP_BASE_PENALTY * factor;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Circuit characteristics context
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Sets circuit characteristics for the current thread.
     *
     * @param asphaltAbrasion  1-5 scale
     * @param tyreStress       1-5 scale
     * @param lateral          1-5 scale
     * @param asphaltGrip      1-5 scale
     * @param softDegScaling   multiplier applied only to soft compound wear
     *                         (1.0 = neutral, 1.8 = Baku blistering risk, 0.82 = Miami)
     */
    public void setCircuitCharacteristics(int asphaltAbrasion, int tyreStress,
                                          int lateral, int asphaltGrip, double softDegScaling) {
        CIRCUIT_CONTEXT.set(new double[] {
                asphaltAbrasion / 3.0,
                tyreStress / 3.0,
                lateral / 3.0,
                asphaltGrip / 3.0,
                softDegScaling
        });
    }

    /** Backward-compatible overload — softDegScaling defaults to 1.0 (neutral). */
    public void setCircuitCharacteristics(int asphaltAbrasion, int tyreStress,
                                          int lateral, int asphaltGrip) {
        setCircuitCharacteristics(asphaltAbrasion, tyreStress, lateral, asphaltGrip, 1.0);
    }

    public void clearCircuitCharacteristics() {
        CIRCUIT_CONTEXT.remove();
    }

    private double getCircuitCharacteristicMultiplier() {
        double[] context = CIRCUIT_CONTEXT.get();
        if (context == null) return 1.0;
        double asphaltAbrasion = context[0];
        double tyreStress = context[1];
        double lateral = context[2];
        return (asphaltAbrasion * 0.40) + (tyreStress * 0.35) + (lateral * 0.25);
    }

    /**
     * Returns the soft compound degradation scaling for the current circuit.
     * Reads index 4 from the ThreadLocal context set via setCircuitCharacteristics.
     */
    private double getSoftCompoundMultiplier() {
        double[] context = CIRCUIT_CONTEXT.get();
        if (context == null || context.length < 5) return 1.0;
        return context[4];
    }
}