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

    /**
     * Starts a live race simulation and sends updates lap-by-lap.
     * Expected payload: { "circuitId": 1, "compoundId": 1 }
     */
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
                            compound.getInitialGrip(),
                            lap);

                    messagingTemplate.convertAndSend("/topic/race-updates", (Object) Map.of(
                            "lap", lap,
                            "lapTime", lapTime,
                            "compound", compound.getName(),
                            "circuit", circuit.getName()));

                    Thread.sleep(500);
                }

                messagingTemplate.convertAndSend("/topic/race-updates", (Object) Map.of("status", "FINISHED"));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                messagingTemplate.convertAndSend("/topic/race-updates",
                        (Object) Map.of("status", "ERROR", "message", "Simulation interrupted"));
            } catch (Exception e) {
                messagingTemplate.convertAndSend("/topic/race-updates",
                        (Object) Map.of("status", "ERROR", "message", e.getMessage()));
            }
        }).start();
    }
}
