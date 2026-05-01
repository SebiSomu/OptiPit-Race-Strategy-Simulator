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
    private Double pitStopLoss;            // pit stop time loss in seconds
    private Double trafficLoss;            // additional loss from traffic after pit (s)
    private Double baseLapTime;            // baseline race lap time in seconds
    private Double trackTempNominal;       // typical track temperature (°C)
    private Double trackEvolutionPerLap;   // track improvement per lap (s)

    // Circuit characteristics (scale 1-5, from F1 track analysis)
    private Integer traction;              // traction level (1=low, 5=high)
    private Integer tyreStress;            // tyre stress level (1=low wear, 5=high wear)
    private Integer asphaltGrip;           // asphalt grip level (1=low, 5=high)
    private Integer braking;               // braking intensity (1=low, 5=high)
    private Integer asphaltAbrasion;       // surface abrasiveness (1=smooth, 5=high wear)
    private Integer lateralForces;         // lateral forces/turns (1=low, 5=high)
    private Integer trackEvolution;        // track evolution/grip buildup (1=low, 5=high)
    private Integer downforce;             // downforce requirements (1=low, 5=high)
}
