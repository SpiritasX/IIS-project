package com.example.iis.repository;

import com.example.iis.model.PlantPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlantPriceRepository extends JpaRepository<PlantPrice, Long> {
    @Query("""
            select price from PlantPrice price
            join fetch price.plant plant
            join fetch plant.variety variety
            join fetch variety.species species
            join fetch species.type type
            join fetch type.category category
            where price.endTime is null
            order by plant.name asc
            """)
    List<PlantPrice> findActiveCatalogPrices();
    Optional<PlantPrice> findByPlant_IdAndEndTimeIsNull(Long plantId);
    Optional<PlantPrice> findTopByPlant_IdAndEndTimeIsNotNullOrderByEndTimeDesc(Long plantId);
    List<PlantPrice> findByPlant_IdOrderByStartTimeAsc(Long Id);
}
