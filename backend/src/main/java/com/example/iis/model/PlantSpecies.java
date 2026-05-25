package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class PlantSpecies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private PlantType type;

    public PlantSpecies() {
    }

    public PlantSpecies(String name, PlantType type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlantType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlantSpecies that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
