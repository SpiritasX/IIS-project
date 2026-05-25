package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class PlantType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private PlantCategory category;

    public PlantType() {
    }

    public PlantType(String name, PlantCategory category) {
        this.name = name;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlantCategory getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlantType plantType)) return false;
        return Objects.equals(id, plantType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
