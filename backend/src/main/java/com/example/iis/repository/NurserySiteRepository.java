package com.example.iis.repository;

import com.example.iis.model.NurserySite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NurserySiteRepository extends JpaRepository<NurserySite, Long> {
    long countByParcel_Id(Long parcelId);

    @Query("SELECT s.parcel.id FROM NurserySite s WHERE s.id = :siteId")
    Optional<Long> findParcelIdBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT s.parcel.unit.id FROM NurserySite s WHERE s.id = :siteId")
    Optional<Long> findUnitIdBySiteId(@Param("siteId") Long siteId);

    List<NurserySite> findByParcel_Id(Long parcelId);
}
