package org.example.backend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stint {
    @ManyToOne
    private TyreCompound tyreCompound;
    
    private Integer startLap;
    private Integer lapsDuration;
}
