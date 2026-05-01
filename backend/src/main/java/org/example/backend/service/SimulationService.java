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
    // Track evolution follows saturation curve: rapid improvement first 5-10 laps,
    // then plateau as track reaches maximum rubbering.
    // Formula: maxEvolution * (1 - exp(-saturationRate * lap))
    private static final double TRACK_EVOLUTION_SATURATION_RATE = 0.35;  // higher = faster plateau
    private static final double MAX_TRACK_EVOLUTION_BONUS = 0.15;        // max total improvement (s)

    // ── Tyre surface temperature constants ───────────────────────────────
    private static final double TYRE_TEMP_OPTIMAL = 90.0;           // optimal tyre surface temp (°C)
    private static final double TYRE_TEMP_BASE = 60.0;              // base temp at start of stint (°C)
    private static final double TYRE_TEMP_PUSH_FACTOR = 8.0;        // temp increase per lap in PUSH mode
    private static final double TYRE_TEMP_NORMAL_FACTOR = 3.0;      // temp increase per lap in NORMAL mode
    private static final double TYRE_TEMP_CONSERVATIVE_FACTOR = 1.0; // temp increase per lap in CONSERVATIVE mode
    private static final double TYRE_TEMP_COOLING = 2.0;          // natural cooling per lap
    private static final double TYRE_DEG_TEMP_MULTIPLIER = 0.02;  // degradation increase per °C above optimal

    // ── Wind constants ───────────────────────────────────────────────────
    private static final double WIND_DRAG_COEFFICIENT = 0.015; // s per km/h of effective headwind

    // ── Air temperature constants ────────────────────────────────────────
    // Cooler air = denser air = more engine power + downforce
    // ~0.008s faster per °C below 30°C reference
    private static final double AIR_TEMP_REFERENCE = 30.0;
    private static final double AIR_TEMP_SENSITIVITY = 0.008;

    // ── Rain constants ───────────────────────────────────────────────────
    private static final double RAIN_PENALTY_BASE = 4.0;    // max penalty per lap at full intensity
    private static final double RAIN_GRIP_MULTIPLIER = 1.5;  // rain accelerates tyre degradation

    // ── Safety Car / VSC constants ─────────────────────────────────────────
    // Safety Car neutralizes race - all cars bunch up at reduced pace
    // Typical SC period: 3-8 laps depending on incident severity
    private static final double SAFETY_CAR_PACE_PENALTY = 25.0;  // seconds slower per lap under SC
    private static final double VSC_PACE_PENALTY = 12.0;       // seconds slower per lap under VSC
    private static final double SAFETY_CAR_PROBABILITY_PER_LAP = 0.02;  // 2% chance per lap for incident
    private static final double VSC_PROBABILITY_PER_LAP = 0.03;         // 3% chance per lap for minor incident

    // ── Tyre warmup constants ────────────────────────────────────────────
    private static final double WARMUP_BASE_PENALTY = 0.5;   // base cold tyre penalty
    private static final double WARMUP_SENSITIVITY = 20.0; // multiplier for temp sensitivity

    // ── Short stint penalty constants (smooth linear model) ─────────────────
    private static final int OPTIMAL_STINT_LENGTH = 12;      // optimal stint length (laps)
    private static final double SHORT_STINT_PENALTY_RATE = 0.05; // +5% penalty per lap under optimal

    // ── Circuit characteristics ThreadLocal for context passing ─────────────
    private static final ThreadLocal<double[]> CIRCUIT_CONTEXT = new ThreadLocal<>();
    // Context array indices: 0=asphaltAbrasion, 1=tyreStress, 2=lateral, 3=asphaltGrip

    // ── Out-lap penalty (cold tyres after pit stop) ─────────────────────────
    private static final double OUT_LAP_BASE_PENALTY = 0.8;  // base penalty for first lap after pit (s)
    private static final double OUT_LAP_SOFT_FACTOR = 1.125; // Soft = 0.9s (high degradation = more heat needed)
    private static final double OUT_LAP_MEDIUM_FACTOR = 0.875; // Medium = 0.7s
    private static final double OUT_LAP_HARD_FACTOR = 0.625; // Hard = 0.5s (low degradation = quick warmup)

    // ── Driving modes ────────────────────────────────────────────────────
    public enum DrivingMode {
        CONSERVATIVE(0.5, 1.0),   // 50% pace, normal degradation
        NORMAL(1.0, 1.0),         // 100% pace, normal degradation
        PUSH(1.02, 1.8);          // 102% pace, 80% more degradation from heat

        final double paceMultiplier;
        final double degMultiplier;

        DrivingMode(double pace, double deg) {
            this.paceMultiplier = pace;
            this.degMultiplier = deg;
        }
    }

    /**
     * Calculates effective degradation for a compound at given conditions.
     */
    public double effectiveDegradation(double baseDeg, double tempSensitivity,
                                        double trackTemp, double rainIntensity,
                                        double wetPerformance, DrivingMode drivingMode) {
        double tempDelta = (trackTemp - NOMINAL_TEMP) / 10.0;
        double baseDegAtTemp = baseDeg + tempSensitivity * tempDelta;

        // Rain increases degradation for slicks, but wet tyres handle it
        double rainEffect = rainIntensity * RAIN_GRIP_MULTIPLIER * (1.0 - wetPerformance);
        double rainMultiplier = 1.0 + rainEffect;
        return baseDegAtTemp * rainMultiplier;
    }

    /**
     * Calculates the lap time for one lap — Physics 4.0 with enhanced accuracy.
     *
     * @param baseLapTime     circuit baseline lap time (s)
     * @param baseDeg         compound base degradation at 35°C (s/lap)
     * @param tempSensitivity compound extra deg per 10°C above nominal (s/lap)
     * @param paceAdvantage   compound pace advantage vs Medium baseline (s)
     * @param lapOnTyre       lap number on THIS tyre set (1, 2, 3…)
     * @param globalLap       actual race lap number (1…totalLaps)
     * @param trackTemp       current track surface temperature (°C)
     * @param trackEvolution  track improvement per lap from rubber (s/lap) - kept for API compatibility
     * @param windSpeed       wind speed in km/h (0 = no wind)
     * @param windAngle       wind angle relative to main straight (0° = headwind, 180° = tailwind)
     * @param airTemp         air temperature in °C
     * @param rainIntensity   0.0 (dry) to 1.0 (heavy rain)
     * @param wetPerformance  0.0 (slick) to 1.0 (full wet) — how well the tyre handles rain
     * @param drivingMode     driving aggression level (CONSERVATIVE/NORMAL/PUSH)
     * @param totalRaceLaps   total number of laps in the race (for fuel calculation)
     */
    public double calculateLapTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int lapOnTyre, int globalLap,
            double trackTemp, double trackEvolution,
            double windSpeed, double windAngle,
            double airTemp, double rainIntensity,
            double wetPerformance, DrivingMode drivingMode, int totalRaceLaps) {

        // ── Tyre surface temperature calculation ──
        // Temperature accumulates based on driving mode, with some natural cooling
        double tempIncreasePerLap;
        switch (drivingMode) {
            case PUSH -> tempIncreasePerLap = TYRE_TEMP_PUSH_FACTOR;
            case CONSERVATIVE -> tempIncreasePerLap = TYRE_TEMP_CONSERVATIVE_FACTOR;
            default -> tempIncreasePerLap = TYRE_TEMP_NORMAL_FACTOR; // NORMAL
        }
        double currentTyreTemp = TYRE_TEMP_BASE + (tempIncreasePerLap - TYRE_TEMP_COOLING) * lapOnTyre;
        currentTyreTemp = Math.min(currentTyreTemp, 120.0); // Cap at 120°C (thermal limit)

        // ── Tyre degradation (non-linear "cliff" with temperature factor) ──
        double degBase = effectiveDegradation(baseDeg, tempSensitivity, trackTemp, rainIntensity, wetPerformance, drivingMode);

        // Temperature effect: degradation accelerates exponentially above optimal temp
        double tempAboveOptimal = Math.max(0, currentTyreTemp - TYRE_TEMP_OPTIMAL);
        double tempDegMultiplier = 1.0 + (TYRE_DEG_TEMP_MULTIPLIER * tempAboveOptimal * drivingMode.degMultiplier);

        // ── Circuit characteristics effect on tyre degradation ──
        // These are passed via ThreadLocal from StrategyOptimizer or default to 1.0
        double circuitDegMultiplier = getCircuitCharacteristicMultiplier();

        // Sweet spot + degradation model with temperature influence:
        // Lap 1-2: Warmup phase - minimal degradation (10% of normal)
        // Lap 3-6: Sweet spot - degradation starts but mild (30% of normal)
        // Lap 7+: Full degradation with non-linear "cliff" and temperature acceleration
        double wearEffect;
        if (lapOnTyre <= 2) {
            wearEffect = degBase * lapOnTyre * 0.1 * tempDegMultiplier * circuitDegMultiplier;
        } else if (lapOnTyre <= 6) {
            wearEffect = degBase * lapOnTyre * 0.3 * tempDegMultiplier * circuitDegMultiplier;
        } else {
            int effectiveLap = lapOnTyre - 6;
            double baseWear = degBase * 6 * 0.3;  // Accumulated from first 6 laps
            wearEffect = (baseWear
                       + (degBase * effectiveLap)           // Linear part
                       + (degBase * 0.15 * Math.pow(effectiveLap, 2.0))) // Steeper cliff (x² instead of x^1.8)
                       * tempDegMultiplier * circuitDegMultiplier;
        }

        // ── Track evolution: track gets faster with saturation curve ──
        // Reality: rapid improvement first 5-10 laps, then plateau
        // Formula: maxEvolution * (1 - exp(-saturationRate * globalLap))
        double saturationFactor = 1.0 - Math.exp(-TRACK_EVOLUTION_SATURATION_RATE * globalLap);
        double trackBonus = MAX_TRACK_EVOLUTION_BONUS * saturationFactor;
        // Rain washes away rubber, reducing track evolution
        if (rainIntensity > 0.3) {
            trackBonus *= (1.0 - rainIntensity * 0.8);
        }

        // ── Fuel effect: scalable mass-dependent model ──
        // Industry standard: ~0.3s per 10kg. With 1.5-2kg/lap consumption = 0.07-0.1s/lap.
        // Effect is stronger at race start when car is heavier.
        double remainingFuel = Math.max(0, MAX_FUEL_CAPACITY - (FUEL_CONSUMPTION_PER_LAP * (globalLap - 1)));
        double fuelPenalty = remainingFuel * FUEL_EFFECT_PER_KG; // s slower due to fuel weight
        double fuelBonus = (MAX_FUEL_CAPACITY * FUEL_EFFECT_PER_KG) - fuelPenalty; // bonus vs start

        // ── Wind effect ──
        // windAngle: 0° = full headwind (worst), 180° = full tailwind (best)
        // cos(0°) = 1.0 (headwind adds time), cos(180°) = -1.0 (tailwind reduces time)
        double windRadians = Math.toRadians(windAngle);
        double effectiveHeadwind = windSpeed * Math.cos(windRadians);
        double windPenalty = effectiveHeadwind * WIND_DRAG_COEFFICIENT;

        // ── Air temperature effect ──
        // Cooler air = denser = more power + downforce
        double airTempBonus = (AIR_TEMP_REFERENCE - airTemp) * AIR_TEMP_SENSITIVITY;

        // ── Rain effect on lap time ──
        // Slick tyres suffer massively; wet tyres are designed for this
        double rainPenalty = rainIntensity * RAIN_PENALTY_BASE * (1.0 - wetPerformance * 0.85);
        // Wet tyres on dry track: extra penalty from excessive rubber contact
        double dryPenaltyForWets = (1.0 - rainIntensity) * wetPerformance * 2.5;

        // ── Tyre warmup effect ──
        // Cold tyres need 1-2 laps to reach optimal temperature
        // Softer compounds = more temp sensitive = slower warmup
        double warmupPenalty = 0.0;
        if (lapOnTyre == 1) {
            // First lap: cold tyres - penalty based on compound softness
            // Soft (high baseDeg) = 0.75s penalty, Medium = 0.45s, Hard = 0.25s
            warmupPenalty = WARMUP_BASE_PENALTY * (1.0 + baseDeg * WARMUP_SENSITIVITY);
        } else if (lapOnTyre == 2) {
            // Second lap: partial warmup - about 40% of first lap penalty
            warmupPenalty = WARMUP_BASE_PENALTY * (1.0 + baseDeg * WARMUP_SENSITIVITY) * 0.4;
        }

        // ── Driving mode pace effect ──
        // PUSH mode gives 2% faster lap times but at cost of higher degradation
        double drivingModePaceBonus = (drivingMode.paceMultiplier - 1.0) * baseLapTime * -1.0;

        return baseLapTime
                - paceAdvantage
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

    /**
     * Backward-compatible overload with default NORMAL driving mode.
     */
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

    /**
     * Backward-compatible 8-parameter overload (defaults: no wind, 25°C air, dry, NORMAL mode).
     */
    public double calculateLapTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int lapOnTyre, int globalLap,
            double trackTemp, double trackEvolution) {
        return calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                lapOnTyre, globalLap, trackTemp, trackEvolution,
                0.0, 0.0, 25.0, 0.0, 0.0, DrivingMode.NORMAL, globalLap);
    }

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

        // ── Smooth stint length penalty for Soft and Medium compounds ──
        // Stints shorter than optimal length get progressive degradation penalty
        // Formula: 8 laps = +20%, 11 laps = +5%, 12+ laps = 0%
        double stintLengthPenalty = (stintLaps < OPTIMAL_STINT_LENGTH)
            ? (1.0 + (OPTIMAL_STINT_LENGTH - stintLaps) * SHORT_STINT_PENALTY_RATE)
            : 1.0;
        if (baseDeg > 0.07) {
            // Soft compound - full penalty
            total *= stintLengthPenalty;
        } else if (baseDeg > 0.035) {
            // Medium compound - half penalty (e.g., 8 laps = +10%, 11 laps = +2.5%)
            double mediumPenalty = 1.0 + (stintLengthPenalty - 1.0) * 0.5;
            total *= mediumPenalty;
        }

        return total;
    }

    /**
     * Backward-compatible stint overload with default NORMAL driving mode.
     */
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

    /**
     * Backward-compatible 8-parameter stint overload.
     */
    public double calculateStintTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int stintLaps, int startGlobalLap,
            double trackTemp, double trackEvolution) {
        return calculateStintTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                stintLaps, startGlobalLap, trackTemp, trackEvolution,
                0.0, 0.0, 25.0, 0.0, 0.0, DrivingMode.NORMAL, startGlobalLap + stintLaps - 1);
    }

    // ── Safety Car / VSC methods ─────────────────────────────────────────

    /**
     * Safety Car event types that can occur during a race.
     */
    public enum SafetyCarEvent {
        NONE,           // No safety car
        VSC,            // Virtual Safety Car (minor incident)
        SAFETY_CAR      // Full Safety Car (major incident)
    }

    /**
     * Determines if a Safety Car or VSC event occurs on a given lap.
     * Uses stochastic probability based on rain and track conditions.
     *
     * @param randomSeed seed for reproducible randomness
     * @param rainIntensity higher rain = higher chance of incidents
     * @param lapNumber current lap number
     * @return type of safety car event
     */
    public SafetyCarEvent checkSafetyCarEvent(long randomSeed, double rainIntensity, int lapNumber) {
        // First lap chaos factor - higher chance of incidents
        double firstLapMultiplier = (lapNumber == 1) ? 2.5 : 1.0;

        // Rain increases incident probability significantly
        double rainMultiplier = 1.0 + (rainIntensity * 2.0);

        // Calculate effective probabilities
        double effectiveVscProb = VSC_PROBABILITY_PER_LAP * rainMultiplier * firstLapMultiplier;
        double effectiveScProb = SAFETY_CAR_PROBABILITY_PER_LAP * rainMultiplier * firstLapMultiplier;

        // Use seeded random for reproducibility
        java.util.Random random = new java.util.Random(randomSeed + lapNumber);
        double roll = random.nextDouble();

        if (roll < effectiveScProb) {
            return SafetyCarEvent.SAFETY_CAR;
        } else if (roll < effectiveScProb + effectiveVscProb) {
            return SafetyCarEvent.VSC;
        }
        return SafetyCarEvent.NONE;
    }

    /**
     * Calculates the time penalty for a lap under Safety Car or VSC conditions.
     *
     * @param baseLapTime normal lap time
     * @param event type of safety car event
     * @return lap time under safety car conditions
     */
    public double applySafetyCarPenalty(double baseLapTime, SafetyCarEvent event) {
        return switch (event) {
            case SAFETY_CAR -> baseLapTime + SAFETY_CAR_PACE_PENALTY;
            case VSC -> baseLapTime + VSC_PACE_PENALTY;
            case NONE -> baseLapTime;
        };
    }

    /**
     * Calculates the out-lap penalty for cold tyres after a pit stop.
     * First lap out of the pits is slower due to cold tyres and caution.
     *
     * @param baseDeg compound degradation coefficient (used to identify compound type)
     * @return penalty in seconds to add to the first lap after pit stop
     */
    public double calculateOutLapPenalty(double baseDeg) {
        double factor;
        if (baseDeg > 0.07) {
            factor = OUT_LAP_SOFT_FACTOR;      // Soft: ~0.9s penalty
        } else if (baseDeg > 0.035) {
            factor = OUT_LAP_MEDIUM_FACTOR;    // Medium: ~0.7s penalty
        } else {
            factor = OUT_LAP_HARD_FACTOR;      // Hard: ~0.5s penalty
        }
        return OUT_LAP_BASE_PENALTY * factor;
    }

    /**
     * Sets the circuit characteristics context for the current thread.
     * This affects tyre degradation calculations in calculateLapTime.
     *
     * @param asphaltAbrasion 1-5 scale (1=smooth, 5=high wear)
     * @param tyreStress 1-5 scale (1=low stress, 5=high stress)
     * @param lateral 1-5 scale (1=low lateral forces, 5=high)
     * @param asphaltGrip 1-5 scale (1=low grip, 5=high grip)
     */
    public void setCircuitCharacteristics(int asphaltAbrasion, int tyreStress, int lateral, int asphaltGrip) {
        CIRCUIT_CONTEXT.set(new double[]{
            asphaltAbrasion / 3.0,  // Normalize to ~0.33-1.67, 1.0 is average
            tyreStress / 3.0,       // Normalize to ~0.33-1.67
            lateral / 3.0,          // Normalize to ~0.33-1.67
            asphaltGrip / 3.0       // Normalize to ~0.33-1.67
        });
    }

    /**
     * Clears the circuit characteristics context.
     * Call this after strategy calculation to prevent memory leaks.
     */
    public void clearCircuitCharacteristics() {
        CIRCUIT_CONTEXT.remove();
    }

    /**
     * Gets the circuit characteristic degradation multiplier.
     * Combines asphalt abrasion, tyre stress, and lateral forces.
     *
     * @return multiplier where 1.0 is average, <1.0 is gentle on tyres, >1.0 is aggressive
     */
    private double getCircuitCharacteristicMultiplier() {
        double[] context = CIRCUIT_CONTEXT.get();
        if (context == null) {
            return 1.0; // No circuit context set, use neutral multiplier
        }

        double asphaltAbrasion = context[0];
        double tyreStress = context[1];
        double lateral = context[2];

        // Formula: abrasion has strongest effect (40%), stress (35%), lateral (25%)
        // Weighted combination gives realistic degradation multiplier
        double multiplier = (asphaltAbrasion * 0.40)
                          + (tyreStress * 0.35)
                          + (lateral * 0.25);

        // Scale so 3,3,3 (all average) gives 1.0 multiplier
        // Range will be approximately 0.67 (1,1,1) to 1.67 (5,5,5)
        return multiplier;
    }
}
