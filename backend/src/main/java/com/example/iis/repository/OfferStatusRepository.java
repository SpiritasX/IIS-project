package com.example.iis.repository;

import com.example.iis.model.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferStatusRepository extends JpaRepository<OfferStatus, Long> {
    Optional<OfferStatus> findByName(String name);
}
