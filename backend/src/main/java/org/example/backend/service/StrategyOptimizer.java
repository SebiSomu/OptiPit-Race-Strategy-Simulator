package org.example.backend.service;

import org.example.backend.dto.LapTimeEntry;
import org.example.backend.dto.StintDetail;
import org.example.backend.dto.StrategyResult;
import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Finds optimal F1 race strategies using precomputed stint time tables.
 *
 * Returns ALL viable compound combinations ranked by total race time,
 * so the user sees every realistic option — not just one per stop type.
 *
 * Results include: all 1-stop compound combos, top 2-stop combos, best 3-stop.
 * Each is independent (different compound sequences may rank in any order).
 *
 * Temperature model:
 *   - trackTemp defaults to circuit.trackTempNominal if not supplied by the user.
 *   - The user can override via query param to simulate hotter/cooler conditions.
 */
@Service
public class StrategyOptimizer {

    @Autowired
    private SimulationService simulationService;

    /**
     * Main entry point: returns all strategies sorted by total race time.
     *
     * @param circuit       the circuit to simulate
     * @param compounds     all available tyre compounds
     * @param trackTemp     actual track temperature (°C) — null = use circuit nominal
     * @param windSpeed     wind speed in km/h (0 = calm)
     * @param windAngle     wind direction relative to main straight (0° = headwind)
     * @param airTemp       air temperature in °C (null = 25°C default)
     * @param rainIntensity 0.0 (dry) to 1.0 (heavy rain)
     */
    public List<StrategyResult> findOptimalStrategies(Circuit circuit,
                                                       List<TyreCompound> compounds,
                                                       Double trackTemp,
                                                       Double windSpeed,
                                                       Double windAngle,
                                                       Double airTemp,
                                                       Double rainIntensity) {
        double temp = (trackTemp != null) ? trackTemp : circuit.getTrackTempNominal();
        double wind = (windSpeed != null) ? windSpeed : 0.0;
        double wAngle = (windAngle != null) ? windAngle : 0.0;
        double aTemp = (airTemp != null) ? airTemp : 25.0;
        double rain = (rainIntensity != null) ? rainIntensity : 0.0;
        int totalLaps = circuit.getLaps();

        simulationService.setCircuitCharacteristics(
            circuit.getAsphaltAbrasion() != null ? circuit.getAsphaltAbrasion() : 3,
            circuit.getTyreStress() != null ? circuit.getTyreStress() : 3,
            circuit.getLateralForces() != null ? circuit.getLateralForces() : 3,
            circuit.getAsphaltGrip() != null ? circuit.getAsphaltGrip() : 3
        );

        // Filter compounds based on rain conditions
        List<TyreCompound> usableCompounds = compounds.stream()
                .filter(c -> {
                    double wetPerf = c.getWetPerformance() != null ? c.getWetPerformance() : 0.0;
                    // In dry conditions, exclude wet tyres (Intermediate and Wet)
                    if (rain == 0.0) {
                        return wetPerf == 0.0;
                    }
                    return true;
                })
                .toList();

        if (usableCompounds.isEmpty()) {
            usableCompounds = compounds;
        }

        double[][][] precomputed = precomputeStintTimes(circuit, usableCompounds, totalLaps,
                temp, wind, wAngle, aTemp, rain);

        List<StrategyResult> all = new ArrayList<>();

        // ── 1-STOP ──
        // 1-stop strategies are forbidden/not viable at Monaco due to overtaking difficulty
        boolean isMonaco = circuit.getName().toLowerCase().contains("monaco");
        if (!isMonaco) {
            for (int c1 = 0; c1 < usableCompounds.size(); c1++) {
                for (int c2 = 0; c2 < usableCompounds.size(); c2++) {
                    if (c1 == c2) continue;
                    StrategyResult r = best1Stop(circuit, usableCompounds, precomputed, temp, totalLaps,
                            c1, c2, wind, wAngle, aTemp, rain);
                    if (r != null) all.add(r);
                }
            }
        }

        // ── 2-STOP ──
        Map<String, StrategyResult> best2Stops = new LinkedHashMap<>();
        for (int c1 = 0; c1 < usableCompounds.size(); c1++) {
            for (int c2 = 0; c2 < usableCompounds.size(); c2++) {
                for (int c3 = 0; c3 < usableCompounds.size(); c3++) {
                    boolean twoCompounds = !(c1 == c2 && c2 == c3);
                    if (!twoCompounds) continue;
                    String key = c1 + "-" + c2 + "-" + c3;
                    StrategyResult r = best2Stop(circuit, usableCompounds, precomputed, temp, totalLaps,
                            c1, c2, c3, wind, wAngle, aTemp, rain);
                    if (r != null) best2Stops.put(key, r);
                }
            }
        }
        best2Stops.values().stream()
                .sorted(Comparator.comparingDouble(StrategyResult::getTotalTime))
                .limit(4)
                .forEach(all::add);

        // ── 3-STOP ──
        List<StrategyResult> best3 = best3Stops(circuit, usableCompounds, precomputed, temp, totalLaps,
                wind, wAngle, aTemp, rain);
        all.addAll(best3);

        all.sort(Comparator.comparingDouble(StrategyResult::getTotalTime));

        if (!all.isEmpty()) {
            double best = all.get(0).getTotalTime();
            for (StrategyResult s : all) {
                s.setDeltaToOptimal(round3(s.getTotalTime() - best));
            }
        }

        // ── Clear circuit characteristics context to prevent memory leaks ──
        simulationService.clearCircuitCharacteristics();

        return all;
    }

    private StrategyResult best1Stop(Circuit circuit, List<TyreCompound> compounds,
                                      double[][][] pre, double temp, int totalLaps,
                                      int c1, int c2,
                                      double wind, double wAngle, double aTemp, double rain) {
        double bestTime = Double.MAX_VALUE;
        int bestPit = -1;

        for (int pit = 3; pit <= totalLaps - 3; pit++) {
            int s1Laps = pit;
            int s2Laps = totalLaps - pit;
            double time = pre[c1][1][s1Laps] + pre[c2][pit + 1][s2Laps] + circuit.getPitStopLoss() + circuit.getTrafficLoss();
            if (time < bestTime) { bestTime = time; bestPit = pit; }
        }
        if (bestPit == -1) return null;

        return buildResult("1-STOP", circuit, compounds, temp, totalLaps,
                new int[]{c1, c2},
                new int[]{1, bestPit + 1},
                new int[]{bestPit, totalLaps},
                List.of(bestPit), bestTime,
                wind, wAngle, aTemp, rain);
    }

    private StrategyResult best2Stop(Circuit circuit, List<TyreCompound> compounds,
                                      double[][][] pre, double temp, int totalLaps,
                                      int c1, int c2, int c3,
                                      double wind, double wAngle, double aTemp, double rain) {
        double bestTime = Double.MAX_VALUE;
        int bPit1 = -1, bPit2 = -1;

        for (int pit1 = 3; pit1 <= totalLaps - 6; pit1++) {
            for (int pit2 = pit1 + 3; pit2 <= totalLaps - 3; pit2++) {
                int s1 = pit1;
                int s2 = pit2 - pit1;
                int s3 = totalLaps - pit2;
                double time = pre[c1][1][s1] + pre[c2][pit1 + 1][s2] + pre[c3][pit2 + 1][s3]
                        + 2 * (circuit.getPitStopLoss() + circuit.getTrafficLoss());
                if (time < bestTime) { bestTime = time; bPit1 = pit1; bPit2 = pit2; }
            }
        }
        if (bPit1 == -1) return null;

        return buildResult("2-STOP", circuit, compounds, temp, totalLaps,
                new int[]{c1, c2, c3},
                new int[]{1, bPit1 + 1, bPit2 + 1},
                new int[]{bPit1, bPit2, totalLaps},
                List.of(bPit1, bPit2), bestTime,
                wind, wAngle, aTemp, rain);
    }

    private List<StrategyResult> best3Stops(Circuit circuit, List<TyreCompound> compounds,
                                             double[][][] pre, double temp, int totalLaps,
                                             double wind, double wAngle, double aTemp, double rain) {
        Map<String, double[]> coarseBest = new LinkedHashMap<>(); // key → {time, pit1, pit2, pit3}

        for (int c1 = 0; c1 < compounds.size(); c1++)
            for (int c2 = 0; c2 < compounds.size(); c2++)
                for (int c3 = 0; c3 < compounds.size(); c3++)
                    for (int c4 = 0; c4 < compounds.size(); c4++) {
                        if (c1 == c2 && c2 == c3 && c3 == c4) continue;
                        String key = c1 + "-" + c2 + "-" + c3 + "-" + c4;
                        double best = Double.MAX_VALUE;
                        double[] state = {best, -1, -1, -1, c1, c2, c3, c4};

                        for (int p1 = 3; p1 <= totalLaps - 9; p1 += 3)
                            for (int p2 = p1 + 3; p2 <= totalLaps - 6; p2 += 3)
                                for (int p3 = p2 + 3; p3 <= totalLaps - 3; p3 += 3) {
                                    int s1 = p1, s2 = p2 - p1, s3 = p3 - p2, s4 = totalLaps - p3;
                                    double t = pre[c1][1][s1] + pre[c2][p1+1][s2]
                                            + pre[c3][p2+1][s3] + pre[c4][p3+1][s4]
                                            + 3 * (circuit.getPitStopLoss() + circuit.getTrafficLoss());
                                    if (t < state[0]) { 
                                        state[0]=t; 
                                        state[1]=p1; 
                                        state[2]=p2; 
                                        state[3]=p3; 
                                    }
                                }
                        if (state[1] >= 0) coarseBest.put(key, state);
                    }

        List<StrategyResult> results = new ArrayList<>();
        coarseBest.values().stream()
                .filter(s -> s[1] >= 0)
                .sorted(Comparator.comparingDouble(s -> s[0]))
                .limit(2)
                .forEach(state -> {
                    int c1=(int)state[4], c2=(int)state[5], c3=(int)state[6], c4=(int)state[7];
                    int bP1=(int)state[1], bP2=(int)state[2], bP3=(int)state[3];
                    double bT = state[0];
                    for (int p1=Math.max(2,bP1-4); p1<=Math.min(totalLaps-9,bP1+4); p1++)
                        for (int p2=Math.max(p1+2,bP2-4); p2<=Math.min(totalLaps-6,bP2+4); p2++)
                            for (int p3=Math.max(p2+2,bP3-4); p3<=Math.min(totalLaps-2,bP3+4); p3++) {
                                int s1=p1, s2=p2-p1, s3=p3-p2, s4=totalLaps-p3;
                                if (s1<1||s2<1||s3<1||s4<1) continue;
                                double t = pre[c1][1][s1]+pre[c2][p1+1][s2]
                                        +pre[c3][p2+1][s3]+pre[c4][p3+1][s4]
                                        +3*(circuit.getPitStopLoss() + circuit.getTrafficLoss());
                                if (t<bT) { 
                                    bT=t;
                                    bP1=p1; 
                                    bP2=p2; 
                                    bP3=p3; 
                                }
                            }

                    StrategyResult r = buildResult("3-STOP", circuit, compounds, temp, totalLaps,
                            new int[]{c1,c2,c3,c4},
                            new int[]{1,bP1+1,bP2+1,bP3+1},
                            new int[]{bP1,bP2,bP3,totalLaps},
                            List.of(bP1,bP2,bP3), bT,
                            wind, wAngle, aTemp, rain);
                    if (r != null) results.add(r);
                });
        return results;
    }

    private double[][][] precomputeStintTimes(Circuit circuit, List<TyreCompound> compounds,
                                               int totalLaps, double trackTemp,
                                               double windSpeed, double windAngle,
                                               double airTemp, double rainIntensity) {
        int n = compounds.size();
        double[][][] table = new double[n][totalLaps + 2][totalLaps + 1];

        for (int ci = 0; ci < n; ci++) {
            TyreCompound c = compounds.get(ci);
            for (int startLap = 1; startLap <= totalLaps; startLap++) {
                table[ci][startLap][0] = 0;
                for (int numLaps = 1; numLaps <= totalLaps - startLap + 1; numLaps++) {
                    int lapOnTyre = numLaps;
                    int globalLap = startLap + numLaps - 1;
                    double lapTime = simulationService.calculateLapTime(
                            circuit.getBaseLapTime(),
                            c.getDegradationCoefficient(),
                            c.getTempSensitivity(),
                            c.getInitialGrip(),
                            lapOnTyre, globalLap,
                            trackTemp,
                            circuit.getTrackEvolutionPerLap(),
                            windSpeed, windAngle, airTemp, rainIntensity,
                            c.getWetPerformance() != null ? c.getWetPerformance() : 0.0);
                    table[ci][startLap][numLaps] = table[ci][startLap][numLaps - 1] + lapTime;
                }
            }
        }
        return table;
    }

    private StrategyResult buildResult(String type, Circuit circuit, List<TyreCompound> compounds,
                                        double trackTemp, int totalLaps,
                                        int[] compIdxs, int[] starts, int[] ends,
                                        List<Integer> pitLaps, double totalTime,
                                        double windSpeed, double windAngle,
                                        double airTemp, double rainIntensity) {
        List<StintDetail> stints = new ArrayList<>();
        List<LapTimeEntry> lapTimes = new ArrayList<>();

        for (int i = 0; i < compIdxs.length; i++) {
            TyreCompound c = compounds.get(compIdxs[i]);
            int startLap = starts[i];
            int endLap = ends[i];
            int numLaps = endLap - startLap + 1;

            double stintTime = 0;
            for (int lapOnTyre = 1; lapOnTyre <= numLaps; lapOnTyre++) {
                int globalLap = startLap + lapOnTyre - 1;
                double lt = simulationService.calculateLapTime(
                        circuit.getBaseLapTime(),
                        c.getDegradationCoefficient(), c.getTempSensitivity(), c.getInitialGrip(),
                        lapOnTyre, globalLap, trackTemp, circuit.getTrackEvolutionPerLap(),
                        windSpeed, windAngle, airTemp, rainIntensity,
                        c.getWetPerformance() != null ? c.getWetPerformance() : 0.0);

                // ── Out-lap penalty for cold tyres after pit stop ──
                // First stint (i==0) starts from grid - no penalty
                // Subsequent stints (i>0) get penalty on their first lap
                if (i > 0 && lapOnTyre == 1) {
                    double outLapPenalty = simulationService.calculateOutLapPenalty(c.getDegradationCoefficient());
                    lt += outLapPenalty;
                }

                stintTime += lt;
                lapTimes.add(new LapTimeEntry(globalLap, round3(lt), c.getName()));
            }
            stints.add(new StintDetail(c.getName(), startLap, endLap, numLaps, round3(stintTime)));
        }

        return new StrategyResult(type, stints, pitLaps, round3(totalTime), 0, lapTimes);
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    public org.example.backend.model.RaceStrategy optimizeOneStop(Circuit circuit,
                                                                   TyreCompound compound1,
                                                                   TyreCompound compound2) {
        int totalLaps = circuit.getLaps();
        double trackTemp = circuit.getTrackTempNominal();
        double bestTime = Double.MAX_VALUE;
        int bestPitLap = 1;
        TyreCompound bestC1 = compound1, bestC2 = compound2;

        for (int pit = 1; pit < totalLaps; pit++) {
            double t1 = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                    compound1.getDegradationCoefficient(), compound1.getTempSensitivity(), compound1.getInitialGrip(),
                    pit, 1, trackTemp, circuit.getTrackEvolutionPerLap());
            double t2 = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                    compound2.getDegradationCoefficient(), compound2.getTempSensitivity(), compound2.getInitialGrip(),
                    totalLaps - pit, pit + 1, trackTemp, circuit.getTrackEvolutionPerLap());
            double total = t1 + t2 + circuit.getPitStopLoss();
            if (total < bestTime) { 
                bestTime = total; 
                bestPitLap = pit; 
                bestC1 = compound1; 
                bestC2 = compound2; 
            }
        }
        for (int pit = 1; pit < totalLaps; pit++) {
            double t1 = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                    compound2.getDegradationCoefficient(), compound2.getTempSensitivity(), compound2.getInitialGrip(),
                    pit, 1, trackTemp, circuit.getTrackEvolutionPerLap());
            double t2 = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                    compound1.getDegradationCoefficient(), compound1.getTempSensitivity(), compound1.getInitialGrip(),
                    totalLaps - pit, pit + 1, trackTemp, circuit.getTrackEvolutionPerLap());
            double total = t1 + t2 + circuit.getPitStopLoss();
            if (total < bestTime) { 
                bestTime = total; 
                bestPitLap = pit; 
                bestC1 = compound2; 
                bestC2 = compound1; 
            }
        }

        org.example.backend.model.RaceStrategy strategy = new org.example.backend.model.RaceStrategy();
        List<org.example.backend.model.Stint> stints = new ArrayList<>();
        stints.add(new org.example.backend.model.Stint(bestC1, 1, bestPitLap));
        stints.add(new org.example.backend.model.Stint(bestC2, bestPitLap + 1, totalLaps - bestPitLap));
        strategy.setStints(stints);
        strategy.setEstimatedTotalTime(bestTime);
        return strategy;
    }
}
