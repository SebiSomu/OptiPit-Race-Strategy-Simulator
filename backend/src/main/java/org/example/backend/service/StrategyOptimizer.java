package org.example.backend.service;

import org.example.backend.dto.LapTimeEntry;
import org.example.backend.dto.StintDetail;
import org.example.backend.dto.StrategyResult;
import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StrategyOptimizer {

    @Autowired
    private SimulationService simulationService;

    /**
     * Finds the top optimal strategies (1-stop, 2-stop, 3-stop) for a given circuit.
     * Uses precomputed stint time lookup tables for performance.
     */
    public List<StrategyResult> findOptimalStrategies(Circuit circuit, List<TyreCompound> compounds) {
        int totalLaps = circuit.getLaps();

        // Precompute stint times: stintTime[compoundIdx][numLaps] = total time for stint of numLaps laps
        double[][] stintTimeTable = precomputeStintTimes(circuit, compounds, totalLaps);

        List<StrategyResult> allStrategies = new ArrayList<>();

        StrategyResult best1Stop = optimizeBest1Stop(circuit, compounds, stintTimeTable, totalLaps);
        if (best1Stop != null) allStrategies.add(best1Stop);

        StrategyResult best2Stop = optimizeBest2Stop(circuit, compounds, stintTimeTable, totalLaps);
        if (best2Stop != null) allStrategies.add(best2Stop);

        StrategyResult best3Stop = optimizeBest3Stop(circuit, compounds, stintTimeTable, totalLaps);
        if (best3Stop != null) allStrategies.add(best3Stop);

        // Sort by total time
        allStrategies.sort(Comparator.comparingDouble(StrategyResult::getTotalTime));

        // Calculate deltas
        if (!allStrategies.isEmpty()) {
            double bestTime = allStrategies.get(0).getTotalTime();
            for (StrategyResult s : allStrategies) {
                s.setDeltaToOptimal(Math.round((s.getTotalTime() - bestTime) * 1000.0) / 1000.0);
            }
        }

        return allStrategies;
    }

    /**
     * Precompute the total stint time for each compound for every possible stint length (1..totalLaps).
     * stintTimeTable[compoundIndex][numLaps] = total time for that stint.
     */
    private double[][] precomputeStintTimes(Circuit circuit, List<TyreCompound> compounds, int totalLaps) {
        double[][] table = new double[compounds.size()][totalLaps + 1];
        for (int ci = 0; ci < compounds.size(); ci++) {
            TyreCompound c = compounds.get(ci);
            table[ci][0] = 0;
            for (int lap = 1; lap <= totalLaps; lap++) {
                double lapTime = simulationService.calculateLapTime(
                        circuit.getBaseLapTime(), c.getDegradationCoefficient(), c.getInitialGrip(), lap);
                table[ci][lap] = table[ci][lap - 1] + lapTime;
            }
        }
        return table;
    }

    /**
     * Optimal 1-stop: O(compounds^2 * laps)
     */
    private StrategyResult optimizeBest1Stop(Circuit circuit, List<TyreCompound> compounds,
                                              double[][] table, int totalLaps) {
        double bestTime = Double.MAX_VALUE;
        int bestPitLap = -1;
        int bestC1 = -1, bestC2 = -1;

        for (int c1 = 0; c1 < compounds.size(); c1++) {
            for (int c2 = 0; c2 < compounds.size(); c2++) {
                if (c1 == c2) continue;
                for (int pitLap = 3; pitLap <= totalLaps - 3; pitLap++) {
                    double time = table[c1][pitLap] + table[c2][totalLaps - pitLap] + circuit.getPitStopLoss();
                    if (time < bestTime) {
                        bestTime = time;
                        bestPitLap = pitLap;
                        bestC1 = c1;
                        bestC2 = c2;
                    }
                }
            }
        }

        if (bestC1 == -1) return null;
        return buildResult("1-STOP", circuit, compounds, table,
                new int[]{bestC1, bestC2},
                new int[]{1, bestPitLap + 1},
                new int[]{bestPitLap, totalLaps},
                List.of(bestPitLap),
                bestTime);
    }

    /**
     * Optimal 2-stop: O(compounds^3 * laps^2) — still fast with precomputed tables
     */
    private StrategyResult optimizeBest2Stop(Circuit circuit, List<TyreCompound> compounds,
                                              double[][] table, int totalLaps) {
        double bestTime = Double.MAX_VALUE;
        int bestPit1 = -1, bestPit2 = -1;
        int bestC1 = -1, bestC2 = -1, bestC3 = -1;

        for (int c1 = 0; c1 < compounds.size(); c1++) {
            for (int c2 = 0; c2 < compounds.size(); c2++) {
                for (int c3 = 0; c3 < compounds.size(); c3++) {
                    if (c1 == c2 && c2 == c3) continue;
                    for (int pit1 = 3; pit1 <= totalLaps - 6; pit1++) {
                        for (int pit2 = pit1 + 3; pit2 <= totalLaps - 3; pit2++) {
                            int stint1Laps = pit1;
                            int stint2Laps = pit2 - pit1;
                            int stint3Laps = totalLaps - pit2;
                            double time = table[c1][stint1Laps] + table[c2][stint2Laps] + table[c3][stint3Laps]
                                    + 2 * circuit.getPitStopLoss();
                            if (time < bestTime) {
                                bestTime = time;
                                bestPit1 = pit1;
                                bestPit2 = pit2;
                                bestC1 = c1;
                                bestC2 = c2;
                                bestC3 = c3;
                            }
                        }
                    }
                }
            }
        }

        if (bestC1 == -1) return null;
        return buildResult("2-STOP", circuit, compounds, table,
                new int[]{bestC1, bestC2, bestC3},
                new int[]{1, bestPit1 + 1, bestPit2 + 1},
                new int[]{bestPit1, bestPit2, totalLaps},
                List.of(bestPit1, bestPit2),
                bestTime);
    }

    /**
     * Optimal 3-stop: Use coarse search (step=3), then refine around the best.
     */
    private StrategyResult optimizeBest3Stop(Circuit circuit, List<TyreCompound> compounds,
                                              double[][] table, int totalLaps) {
        double bestTime = Double.MAX_VALUE;
        int bestPit1 = -1, bestPit2 = -1, bestPit3 = -1;
        int bestC1 = -1, bestC2 = -1, bestC3 = -1, bestC4 = -1;

        // Coarse search with step=3
        for (int c1 = 0; c1 < compounds.size(); c1++) {
            for (int c2 = 0; c2 < compounds.size(); c2++) {
                for (int c3 = 0; c3 < compounds.size(); c3++) {
                    for (int c4 = 0; c4 < compounds.size(); c4++) {
                        if (c1 == c2 && c2 == c3 && c3 == c4) continue;

                        for (int pit1 = 3; pit1 <= totalLaps - 9; pit1 += 3) {
                            for (int pit2 = pit1 + 3; pit2 <= totalLaps - 6; pit2 += 3) {
                                for (int pit3 = pit2 + 3; pit3 <= totalLaps - 3; pit3 += 3) {
                                    int s1 = pit1;
                                    int s2 = pit2 - pit1;
                                    int s3 = pit3 - pit2;
                                    int s4 = totalLaps - pit3;
                                    double time = table[c1][s1] + table[c2][s2] + table[c3][s3] + table[c4][s4]
                                            + 3 * circuit.getPitStopLoss();
                                    if (time < bestTime) {
                                        bestTime = time;
                                        bestPit1 = pit1;
                                        bestPit2 = pit2;
                                        bestPit3 = pit3;
                                        bestC1 = c1;
                                        bestC2 = c2;
                                        bestC3 = c3;
                                        bestC4 = c4;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestC1 == -1) return null;

        // Fine-grained refinement around best found
        for (int pit1 = Math.max(2, bestPit1 - 3); pit1 <= Math.min(totalLaps - 9, bestPit1 + 3); pit1++) {
            for (int pit2 = Math.max(pit1 + 2, bestPit2 - 3); pit2 <= Math.min(totalLaps - 6, bestPit2 + 3); pit2++) {
                for (int pit3 = Math.max(pit2 + 2, bestPit3 - 3); pit3 <= Math.min(totalLaps - 2, bestPit3 + 3); pit3++) {
                    int s1 = pit1;
                    int s2 = pit2 - pit1;
                    int s3 = pit3 - pit2;
                    int s4 = totalLaps - pit3;
                    if (s1 < 1 || s2 < 1 || s3 < 1 || s4 < 1) continue;
                    double time = table[bestC1][s1] + table[bestC2][s2] + table[bestC3][s3] + table[bestC4][s4]
                            + 3 * circuit.getPitStopLoss();
                    if (time < bestTime) {
                        bestTime = time;
                        bestPit1 = pit1;
                        bestPit2 = pit2;
                        bestPit3 = pit3;
                    }
                }
            }
        }

        return buildResult("3-STOP", circuit, compounds, table,
                new int[]{bestC1, bestC2, bestC3, bestC4},
                new int[]{1, bestPit1 + 1, bestPit2 + 1, bestPit3 + 1},
                new int[]{bestPit1, bestPit2, bestPit3, totalLaps},
                List.of(bestPit1, bestPit2, bestPit3),
                bestTime);
    }

    // ─── Builder Helpers ────────────────────────────────────────────

    private StrategyResult buildResult(String type, Circuit circuit, List<TyreCompound> compounds,
                                        double[][] table, int[] compoundIdxs, int[] starts, int[] ends,
                                        List<Integer> pitStopLaps, double totalTime) {
        List<StintDetail> stints = new ArrayList<>();
        List<LapTimeEntry> lapTimes = new ArrayList<>();

        for (int i = 0; i < compoundIdxs.length; i++) {
            TyreCompound c = compounds.get(compoundIdxs[i]);
            int startLap = starts[i];
            int endLap = ends[i];
            int numLaps = endLap - startLap + 1;
            double stintTime = table[compoundIdxs[i]][numLaps];

            stints.add(new StintDetail(c.getName(), startLap, endLap, numLaps, Math.round(stintTime * 1000.0) / 1000.0));

            // Build per-lap times
            for (int lapOnTyre = 1; lapOnTyre <= numLaps; lapOnTyre++) {
                double lapTime = simulationService.calculateLapTime(
                        circuit.getBaseLapTime(), c.getDegradationCoefficient(), c.getInitialGrip(), lapOnTyre);
                lapTimes.add(new LapTimeEntry(startLap + lapOnTyre - 1, Math.round(lapTime * 1000.0) / 1000.0, c.getName()));
            }
        }

        return new StrategyResult(type, stints, pitStopLaps, Math.round(totalTime * 1000.0) / 1000.0, 0, lapTimes);
    }

    // ─── Legacy Method ──────────────────────────────────────────────

    public org.example.backend.model.RaceStrategy optimizeOneStop(Circuit circuit, TyreCompound compound1, TyreCompound compound2) {
        int totalLaps = circuit.getLaps();
        double bestTime = Double.MAX_VALUE;
        int bestPitLap = 1;
        TyreCompound bestStartCompound = compound1;
        TyreCompound bestEndCompound = compound2;

        for (int pitLap = 1; pitLap < totalLaps; pitLap++) {
            double time = calculateOneStopTimeLegacy(circuit, compound1, compound2, pitLap);
            if (time < bestTime) {
                bestTime = time;
                bestPitLap = pitLap;
                bestStartCompound = compound1;
                bestEndCompound = compound2;
            }
        }

        for (int pitLap = 1; pitLap < totalLaps; pitLap++) {
            double time = calculateOneStopTimeLegacy(circuit, compound2, compound1, pitLap);
            if (time < bestTime) {
                bestTime = time;
                bestPitLap = pitLap;
                bestStartCompound = compound2;
                bestEndCompound = compound1;
            }
        }

        org.example.backend.model.RaceStrategy strategy = new org.example.backend.model.RaceStrategy();
        List<org.example.backend.model.Stint> stints = new ArrayList<>();
        stints.add(new org.example.backend.model.Stint(bestStartCompound, 1, bestPitLap));
        stints.add(new org.example.backend.model.Stint(bestEndCompound, bestPitLap + 1, totalLaps - bestPitLap));

        strategy.setStints(stints);
        strategy.setEstimatedTotalTime(bestTime);

        return strategy;
    }

    private double calculateOneStopTimeLegacy(Circuit circuit, TyreCompound c1, TyreCompound c2, int pitLap) {
        double stint1Time = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                c1.getDegradationCoefficient(), c1.getInitialGrip(), pitLap);
        double stint2Time = simulationService.calculateStintTime(circuit.getBaseLapTime(),
                c2.getDegradationCoefficient(), c2.getInitialGrip(), circuit.getLaps() - pitLap);
        return stint1Time + stint2Time + circuit.getPitStopLoss();
    }
}
