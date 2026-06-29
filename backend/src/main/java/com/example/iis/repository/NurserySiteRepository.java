package com.example.iis.repository;

import com.example.iis.model.NurserySite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NurserySiteRepository extends JpaRepository<NurserySite, Long> {
    boolean existsByName(String name);
}
