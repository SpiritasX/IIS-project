package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class PlantVariety {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private Double humidity;
    private String soil;
    private String instructions;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private PlantSpecies species;

    public PlantVariety() {
    }

    public PlantVariety(String name, Double humidity, String soil, String instructions, PlantSpecies species) {
        this.name = name;
        this.humidity = humidity;
        this.soil = soil;
        this.instructions = instructions;
        this.species = species;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public PlantSpecies getSpecies() {
        return species;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlantVariety that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
