package org.example.backend.controller;

import org.example.backend.model.Circuit;
import org.example.backend.model.TyreCompound;
import org.example.backend.repository.CircuitRepository;
import org.example.backend.repository.TyreCompoundRepository;
import org.example.backend.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SimulationWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private CircuitRepository circuitRepository;

    @Autowired
    private TyreCompoundRepository tyreCompoundRepository;

    @MessageMapping("/start-simulation")
    public void startSimulation(Map<String, Long> params) {
        Long circuitId = params.get("circuitId");
        Long compoundId = params.get("compoundId");

        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        TyreCompound compound = tyreCompoundRepository.findById(compoundId)
                .orElseThrow(() -> new RuntimeException("Compound not found"));

        new Thread(() -> {
            try {
                for (int lap = 1; lap <= circuit.getLaps(); lap++) {
                    double lapTime = simulationService.calculateLapTime(
                            circuit.getBaseLapTime(),
                            compound.getDegradationCoefficient(),
                            compound.getTempSensitivity(),
                            compound.getInitialGrip(),
                            lap,        // lapOnTyre
                            lap,        // globalLap
                            circuit.getTrackTempNominal(),
                            circuit.getTrackEvolutionPerLap()
                    );

                    messagingTemplate.convertAndSend("/topic/race-updates", (Object) Map.of(
                            "lap", lap,
                            "lapTime", Math.round(lapTime * 1000.0) / 1000.0,
                            "compound", compound.getName(),
                            "circuit", circuit.getName()));

                    Thread.sleep(300); 
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
