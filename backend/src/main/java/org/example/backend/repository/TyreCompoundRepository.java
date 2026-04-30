package org.example.backend.repository;

import org.example.backend.model.TyreCompound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TyreCompoundRepository extends JpaRepository<TyreCompound, Long> {
}
