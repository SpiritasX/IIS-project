package com.example.iis.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private LocationUnit locationType;

    @OneToMany(mappedBy = "variety")
    private Set<Plant> plants = new LinkedHashSet<>();

    public PlantVariety() {
    }

    public PlantVariety(String name, Double humidity, String soil, String instructions, PlantSpecies species) {
        this(name, humidity, soil, instructions, species, null);
    }

    public PlantVariety(String name, Double humidity, String soil, String instructions, PlantSpecies species, LocationUnit locationType) {
        this.name = name;
        this.humidity = humidity;
        this.soil = soil;
        this.instructions = instructions;
        this.species = species;
        this.locationType = locationType;
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

    public void setSpecies(PlantSpecies species) {
        this.species = species;
    }

    public LocationUnit getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationUnit locationType) {
        this.locationType = locationType;
    }

    public Set<Plant> getPlants() {
        return plants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantVariety that = (PlantVariety) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
