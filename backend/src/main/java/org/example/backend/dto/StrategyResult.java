package org.example.backend.dto;

import java.util.List;

public class StrategyResult {
    private String strategyType; // "1-STOP", "2-STOP", "3-STOP"
    private List<StintDetail> stints;
    private List<Integer> pitStopLaps;
    private double totalTime;
    private double deltaToOptimal;
    private List<LapTimeEntry> lapTimes;

    public StrategyResult() {}

    public StrategyResult(String strategyType, List<StintDetail> stints, List<Integer> pitStopLaps,
                          double totalTime, double deltaToOptimal, List<LapTimeEntry> lapTimes) {
        this.strategyType = strategyType;
        this.stints = stints;
        this.pitStopLaps = pitStopLaps;
        this.totalTime = totalTime;
        this.deltaToOptimal = deltaToOptimal;
        this.lapTimes = lapTimes;
    }

    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public List<StintDetail> getStints() { return stints; }
    public void setStints(List<StintDetail> stints) { this.stints = stints; }
    public List<Integer> getPitStopLaps() { return pitStopLaps; }
    public void setPitStopLaps(List<Integer> pitStopLaps) { this.pitStopLaps = pitStopLaps; }
    public double getTotalTime() { return totalTime; }
    public void setTotalTime(double totalTime) { this.totalTime = totalTime; }
    public double getDeltaToOptimal() { return deltaToOptimal; }
    public void setDeltaToOptimal(double deltaToOptimal) { this.deltaToOptimal = deltaToOptimal; }
    public List<LapTimeEntry> getLapTimes() { return lapTimes; }
    public void setLapTimes(List<LapTimeEntry> lapTimes) { this.lapTimes = lapTimes; }
}
