package com.example.iis.repository;

import com.example.iis.model.StorageSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageSpaceRepository extends JpaRepository<StorageSpace, Long> {
    List<StorageSpace> findByType(String type);

    List<StorageSpace> findByNurserySite_Id(Long nurserySiteId);

    boolean existsByNurserySite_Id(Long nurserySiteId);
}
