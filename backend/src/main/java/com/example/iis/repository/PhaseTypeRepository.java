package com.example.iis.repository;

import com.example.iis.model.PhaseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhaseTypeRepository extends JpaRepository<PhaseType, Long> {
    Optional<PhaseType> findByName(String name);
}
