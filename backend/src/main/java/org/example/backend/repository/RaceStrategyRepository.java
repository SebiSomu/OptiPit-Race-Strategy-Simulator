package org.example.backend.repository;

import org.example.backend.model.RaceStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceStrategyRepository extends JpaRepository<RaceStrategy, Long> {
}
