package org.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    public double calculateLapTime(double baseLapTime, double degradationCoefficient, double initialGrip,
            int lapOnTyre) {
        return (baseLapTime * (2.0 - initialGrip)) + (degradationCoefficient * Math.pow(lapOnTyre, 2));
    }

    public double calculateStintTime(double baseLapTime, double degradationCoefficient, double initialGrip,
            int lapsInStint) {
        double totalTime = 0;
        for (int i = 1; i <= lapsInStint; i++) {
            totalTime += calculateLapTime(baseLapTime, degradationCoefficient, initialGrip, i);
        }
        return totalTime;
    }
}
