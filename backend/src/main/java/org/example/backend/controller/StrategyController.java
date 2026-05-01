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

    /** Returns all circuits. */
    @GetMapping("/circuits")
    public List<Circuit> getCircuits() {
        return circuitRepository.findAll();
    }

    /** Returns all tyre compounds. */
    @GetMapping("/compounds")
    public List<TyreCompound> getCompounds() {
        return tyreCompoundRepository.findAll();
    }

    /**
     * Calculates all viable strategies sorted by total race time.
     * Physics 3.0: supports weather parameters.
     *
     * @param circuitId      required
     * @param trackTemp      optional — overrides circuit's nominal track temperature (°C)
     * @param windSpeed      optional — wind speed in km/h (default: 0)
     * @param windAngle      optional — wind direction: 0°=headwind, 180°=tailwind (default: 0)
     * @param airTemp        optional — air temperature in °C (default: 25)
     * @param rainIntensity  optional — 0.0 (dry) to 1.0 (heavy rain) (default: 0)
     */
    @GetMapping("/strategy/optimal")
    public List<StrategyResult> getOptimalStrategies(
            @RequestParam Long circuitId,
            @RequestParam(required = false) Double trackTemp,
            @RequestParam(required = false) Double windSpeed,
            @RequestParam(required = false) Double windAngle,
            @RequestParam(required = false) Double airTemp,
            @RequestParam(required = false) Double rainIntensity) {

        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitId));
        List<TyreCompound> compounds = tyreCompoundRepository.findAll();

        return strategyOptimizer.findOptimalStrategies(circuit, compounds,
                trackTemp, windSpeed, windAngle, airTemp, rainIntensity);
    }

    /** Legacy 1-stop endpoint (backward compatibility). */
    @GetMapping("/strategy/calculate")
    public RaceStrategy calculate(
            @RequestParam Long circuitId,
            @RequestParam Long compound1Id,
            @RequestParam Long compound2Id) {

        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitId));
        TyreCompound c1 = tyreCompoundRepository.findById(compound1Id)
                .orElseThrow(() -> new RuntimeException("Compound not found: " + compound1Id));
        TyreCompound c2 = tyreCompoundRepository.findById(compound2Id)
                .orElseThrow(() -> new RuntimeException("Compound not found: " + compound2Id));

        return strategyOptimizer.optimizeOneStop(circuit, c1, c2);
    }
}
