package org.example.backend.repository;

import org.example.backend.model.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, Long> {
    Optional<Circuit> findByName(String name);
}
