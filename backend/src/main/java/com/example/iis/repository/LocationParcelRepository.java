package com.example.iis.repository;

import com.example.iis.model.LocationParcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationParcelRepository extends JpaRepository<LocationParcel, Long> {
    Optional<LocationParcel> findByName(String name);
}
