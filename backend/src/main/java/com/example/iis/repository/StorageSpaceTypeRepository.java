package com.example.iis.repository;

import com.example.iis.model.StorageSpaceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageSpaceTypeRepository extends JpaRepository<StorageSpaceType, Long> {
    boolean existsByName(String name);
}
