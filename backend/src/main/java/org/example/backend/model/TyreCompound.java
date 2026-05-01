package org.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a tyre compound with realistic F1 degradation characteristics.
 *
 * Fields:
 *  - paceAdvantage (initialGrip): pace gap vs Medium baseline in seconds
 *                                 (positive = faster, negative = slower)
 *  - degradationCoefficient:      base linear degradation at 35°C standard temp (s/lap)
 *  - tempSensitivity:             extra degradation per 10°C above 35°C nominal (s/lap)
 *
 * Effective degradation formula:
 *   effectiveDeg = degradationCoefficient + tempSensitivity × (trackTemp - 35) / 10
 *   lapTime = baseLapTime - paceAdvantage + effectiveDeg × lapOnTyre
 *             - trackEvolution × (globalLap - 1)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TyreCompound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;                    // Soft / Medium / Hard / Intermediate / Wet
    private Double degradationCoefficient;  // base deg at 35°C (s/lap)
    private Double initialGrip;             // pace advantage vs Medium (s) - positive=faster
    private Double tempSensitivity;         // extra deg per 10°C above nominal (s/lap)
    private Double wetPerformance;          // 0.0 = slick (full rain penalty), 1.0 = full wet (no rain penalty)
}
