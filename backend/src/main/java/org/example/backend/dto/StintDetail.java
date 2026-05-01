package org.example.backend.dto;

public class StintDetail {
    private String compoundName;
    private int startLap;
    private int endLap;
    private int lapsDuration;
    private double stintTime;

    public StintDetail() {
    }

    public StintDetail(String compoundName, int startLap, int endLap, int lapsDuration, double stintTime) {
        this.compoundName = compoundName;
        this.startLap = startLap;
        this.endLap = endLap;
        this.lapsDuration = lapsDuration;
        this.stintTime = stintTime;
    }

    public String getCompoundName() {
        return compoundName;
    }

    public void setCompoundName(String compoundName) {
        this.compoundName = compoundName;
    }

    public int getStartLap() {
        return startLap;
    }

    public void setStartLap(int startLap) {
        this.startLap = startLap;
    }

    public int getEndLap() {
        return endLap;
    }

    public void setEndLap(int endLap) {
        this.endLap = endLap;
    }

    public int getLapsDuration() {
        return lapsDuration;
    }

    public void setLapsDuration(int lapsDuration) {
        this.lapsDuration = lapsDuration;
    }

    public double getStintTime() {
        return stintTime;
    }

    public void setStintTime(double stintTime) {
        this.stintTime = stintTime;
    }
}
