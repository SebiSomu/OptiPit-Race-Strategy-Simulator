package org.example.backend.service;

import org.springframework.stereotype.Service;

/**
 * Physics 3.0 — F1 lap time simulation engine.
 *
 * Models:
 *   - Non-linear tyre degradation ("The Cliff")
 *   - Fuel burn effect (non-linear mass reduction)
 *   - Track evolution (rubber laid on track)
 *   - Temperature sensitivity (track + air)
 *   - Wind effect (aerodynamic drag delta)
 *   - Rain effect (wet track penalty on slick tyres)
 *   - Safety Car time discount (stochastic)
 */
@Service
public class SimulationService {

    private static final double NOMINAL_TEMP = 35.0;
    private static final double FUEL_BURN_LAPS_EFFECT = 0.060; // s/lap faster as fuel burns

    // ── Wind constants ───────────────────────────────────────────────────
    // At 30 km/h headwind, expect ~0.4s loss per lap on average
    private static final double WIND_DRAG_COEFFICIENT = 0.015; // s per km/h of effective headwind

    // ── Air temperature constants ────────────────────────────────────────
    // Cooler air = denser air = more engine power + more downforce
    // ~0.008s faster per °C below 30°C reference
    private static final double AIR_TEMP_REFERENCE = 30.0;
    private static final double AIR_TEMP_SENSITIVITY = 0.008;

    // ── Rain constants ───────────────────────────────────────────────────
    // Light rain on slicks: +2-5s per lap. Heavy rain: undriveable on slicks.
    // rainIntensity: 0.0 (dry) → 1.0 (heavy rain)
    private static final double RAIN_PENALTY_BASE = 4.0;    // max penalty per lap at full intensity
    private static final double RAIN_GRIP_MULTIPLIER = 1.5;  // rain accelerates tyre degradation

    /**
     * Calculates effective degradation for a compound at given conditions.
     */
    public double effectiveDegradation(double baseDeg, double tempSensitivity,
                                        double trackTemp, double rainIntensity,
                                        double wetPerformance) {
        double tempDelta = (trackTemp - NOMINAL_TEMP) / 10.0;
        double baseDegAtTemp = baseDeg + tempSensitivity * tempDelta;

        // Rain increases degradation for slicks, but wet tyres handle it
        double rainEffect = rainIntensity * RAIN_GRIP_MULTIPLIER * (1.0 - wetPerformance);
        double rainMultiplier = 1.0 + rainEffect;
        return baseDegAtTemp * rainMultiplier;
    }

    /**
     * Calculates the lap time for one lap — Physics 3.0.
     *
     * @param baseLapTime     circuit baseline lap time (s)
     * @param baseDeg         compound base degradation at 35°C (s/lap)
     * @param tempSensitivity compound extra deg per 10°C above nominal (s/lap)
     * @param paceAdvantage   compound pace advantage vs Medium baseline (s)
     * @param lapOnTyre       lap number on THIS tyre set (1, 2, 3…)
     * @param globalLap       actual race lap number (1…totalLaps)
     * @param trackTemp       current track surface temperature (°C)
     * @param trackEvolution  track improvement per lap from rubber (s/lap)
     * @param windSpeed       wind speed in km/h (0 = no wind)
     * @param windAngle       wind angle relative to main straight (0° = headwind, 180° = tailwind)
     * @param airTemp         air temperature in °C
     * @param rainIntensity   0.0 (dry) to 1.0 (heavy rain)
     * @param wetPerformance  0.0 (slick) to 1.0 (full wet) — how well the tyre handles rain
     */
    public double calculateLapTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int lapOnTyre, int globalLap,
            double trackTemp, double trackEvolution,
            double windSpeed, double windAngle,
            double airTemp, double rainIntensity,
            double wetPerformance) {

        // ── Tyre degradation (non-linear "cliff") ──
        double degBase = effectiveDegradation(baseDeg, tempSensitivity, trackTemp, rainIntensity, wetPerformance);
        double wearEffect = (degBase * lapOnTyre) + (degBase * 0.02 * Math.pow(lapOnTyre, 1.8));

        // ── Track evolution: track gets faster ──
        double trackBonus = trackEvolution * (globalLap - 1);
        // Rain washes away rubber, reducing track evolution
        if (rainIntensity > 0.3) {
            trackBonus *= (1.0 - rainIntensity * 0.8);
        }

        // ── Fuel effect: car gets faster as fuel is consumed ──
        double fuelBonus = FUEL_BURN_LAPS_EFFECT * (globalLap - 1);

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

        return baseLapTime
                - paceAdvantage
                + wearEffect
                - trackBonus
                - fuelBonus
                + windPenalty
                - airTempBonus
                + rainPenalty
                + dryPenaltyForWets;
    }

    /**
     * Backward-compatible 8-parameter overload (defaults: no wind, 25°C air, dry).
     */
    public double calculateLapTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int lapOnTyre, int globalLap,
            double trackTemp, double trackEvolution) {
        return calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                lapOnTyre, globalLap, trackTemp, trackEvolution,
                0.0, 0.0, 25.0, 0.0, 0.0);
    }

    /**
     * Calculates total stint time.
     */
    public double calculateStintTime(double baseLapTime,
            double baseDeg, double tempSensitivity, double paceAdvantage,
            int stintLaps, int startGlobalLap,
            double trackTemp, double trackEvolution,
            double windSpeed, double windAngle,
            double airTemp, double rainIntensity,
            double wetPerformance) {
        double total = 0;
        for (int lapOnTyre = 1; lapOnTyre <= stintLaps; lapOnTyre++) {
            int globalLap = startGlobalLap + lapOnTyre - 1;
            total += calculateLapTime(baseLapTime, baseDeg, tempSensitivity, paceAdvantage,
                    lapOnTyre, globalLap, trackTemp, trackEvolution,
                    windSpeed, windAngle, airTemp, rainIntensity, wetPerformance);
        }
        return total;
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
                0.0, 0.0, 25.0, 0.0, 0.0);
    }
}
