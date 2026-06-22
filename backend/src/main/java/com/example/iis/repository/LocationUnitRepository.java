package com.example.iis.repository;

import com.example.iis.model.LocationUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationUnitRepository extends JpaRepository<LocationUnit, Long> {
    List<LocationUnit> findByType(String type);
}
