package org.example.backend.controller;

import org.example.backend.dto.StrategyResult;
import org.example.backend.model.Circuit;
import org.example.backend.model.RaceStrategy;
import org.example.backend.model.TyreCompound;
import org.example.backend.service.StrategyOptimizer;
import org.example.backend.repository.CircuitRepository;
import org.example.backend.repository.TyreCompoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StrategyController {

    @Autowired
    private StrategyOptimizer strategyOptimizer;

    @Autowired
    private CircuitRepository circuitRepository;

    @Autowired
    private TyreCompoundRepository tyreCompoundRepository;

    /**
     * Returns all available circuits.
     */
    @GetMapping("/circuits")
    public List<Circuit> getCircuits() {
        return circuitRepository.findAll();
    }

    /**
     * Returns all available tyre compounds.
     */
    @GetMapping("/compounds")
    public List<TyreCompound> getCompounds() {
        return tyreCompoundRepository.findAll();
    }

    /**
     * Calculates optimal strategies (1-stop, 2-stop, 3-stop) for a given circuit,
     * ranked by total race time.
     */
    @GetMapping("/strategy/optimal")
    public List<StrategyResult> getOptimalStrategies(@RequestParam Long circuitId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found with ID: " + circuitId));
        List<TyreCompound> compounds = tyreCompoundRepository.findAll();

        return strategyOptimizer.findOptimalStrategies(circuit, compounds);
    }

    /**
     * Legacy endpoint: calculates a single 1-stop strategy for two specific compounds.
     */
    @GetMapping("/strategy/calculate")
    public RaceStrategy calculate(
            @RequestParam Long circuitId,
            @RequestParam Long compound1Id,
            @RequestParam Long compound2Id) {

        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found with ID: " + circuitId));
        TyreCompound c1 = tyreCompoundRepository.findById(compound1Id)
                .orElseThrow(() -> new RuntimeException("Compound 1 not found with ID: " + compound1Id));
        TyreCompound c2 = tyreCompoundRepository.findById(compound2Id)
                .orElseThrow(() -> new RuntimeException("Compound 2 not found with ID: " + compound2Id));

        return strategyOptimizer.optimizeOneStop(circuit, c1, c2);
    }
}
