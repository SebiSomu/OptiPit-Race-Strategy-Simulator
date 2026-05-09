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

        @GetMapping("/circuits")
        public List<Circuit> getCircuits() {
                return circuitRepository.findAll();
        }

        @GetMapping("/compounds")
        public List<TyreCompound> getCompounds() {
                return tyreCompoundRepository.findAll();
        }

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

                List<StrategyResult> results = strategyOptimizer.findOptimalStrategies(circuit, compounds,
                                trackTemp, windSpeed, windAngle, airTemp, rainIntensity);

                return results.size() > 20 ? results.subList(0, 20) : results;
        }

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
