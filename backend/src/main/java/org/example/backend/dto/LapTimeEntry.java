package org.example.backend.dto;

public class LapTimeEntry {
    private int lap;
    private double lapTime;
    private String compound;

    public LapTimeEntry() {}

    public LapTimeEntry(int lap, double lapTime, String compound) {
        this.lap = lap;
        this.lapTime = lapTime;
        this.compound = compound;
    }

    public int getLap() { return lap; }
    public void setLap(int lap) { this.lap = lap; }
    public double getLapTime() { return lapTime; }
    public void setLapTime(double lapTime) { this.lapTime = lapTime; }
    public String getCompound() { return compound; }
    public void setCompound(String compound) { this.compound = compound; }
}
