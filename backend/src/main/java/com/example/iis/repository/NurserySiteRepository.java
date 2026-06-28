package com.example.iis.repository;

import com.example.iis.model.NurserySite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NurserySiteRepository extends JpaRepository<NurserySite, Long> {
    long countBySector_Id(Long sectorId);

    @Query("SELECT s.sector.id FROM NurserySite s WHERE s.id = :siteId")
    Optional<Long> findSectorIdBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT s.sector.storageSpace.id FROM NurserySite s WHERE s.id = :siteId")
    Optional<Long> findStorageSpaceIdBySiteId(@Param("siteId") Long siteId);

    List<NurserySite> findBySector_Id(Long sectorId);
}
