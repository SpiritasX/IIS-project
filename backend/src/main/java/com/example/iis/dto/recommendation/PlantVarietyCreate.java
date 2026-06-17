package com.example.iis.dto.recommendation;

public class PlantVarietyCreate {
    public Long id;
    public String name;
    public String season;

    public PlantVarietyCreate(Long id, String name, String season) {
        this.id = id;
        this.name = name;
        this.season = season;
    }
}
