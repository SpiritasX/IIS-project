package com.example.iis.dto.recommendation;

public class PlantCreate {
    public Long id;
    public String name;
    public Long plant_variety_id;

    public PlantCreate(Long id, String name, Long plantVarietyId) {
        this.id = id;
        this.name = name;
        this.plant_variety_id = plantVarietyId;
    }
}
