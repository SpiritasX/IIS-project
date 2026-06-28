package com.example.iis.repository;

import com.example.iis.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByName(String name);

    List<Sector> findByStorageSpace_Id(Long storageSpaceId);
}
