package org.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;
    private Integer laps;
    private Double pitStopLoss;
    private Double trafficLoss;
    private Double baseLapTime;
    private Double trackTempNominal;
    private Double trackEvolutionPerLap;

    // Circuit characteristics (scale 1-5, from F1 track analysis)
    private Integer traction;
    private Integer tyreStress;
    private Integer asphaltGrip;
    private Integer braking;
    private Integer asphaltAbrasion;
    private Integer lateralForces;
    private Integer trackEvolution;
    private Integer downforce;

    private String tyreNomination; // e.g. "C1-C2-C3"
    private Double softDegScaling;
}