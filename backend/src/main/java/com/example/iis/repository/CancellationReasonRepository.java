package com.example.iis.repository;

import com.example.iis.model.CancellationReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancellationReasonRepository extends JpaRepository<CancellationReason, Long> {
    Optional<CancellationReason> findByName(String name);
}
