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
}
