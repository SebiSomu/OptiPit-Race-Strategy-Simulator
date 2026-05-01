package org.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents an F1 circuit with its race and environmental characteristics.
 *
 * trackTempNominal:       typical track surface temperature (°C) for this venue
 *                         Used as default if user doesn't specify one.
 *
 * trackEvolutionPerLap:   lap time improvement (s/lap) from rubber laid on track.
 *                         E.g. 0.010 means each additional race lap the base pace
 *                         improves by 10ms due to track rubbering-in.
 *                         Typical: 0.008–0.015 depending on circuit + conditions.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Circuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer laps;
    private Double pitStopLoss;            // pit stop time loss in seconds
    private Double baseLapTime;            // baseline race lap time in seconds
    private Double trackTempNominal;       // typical track temperature (°C)
    private Double trackEvolutionPerLap;   // track improvement per lap (s)
}
