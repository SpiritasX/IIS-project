package com.example.iis.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class PlantType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private PlantCategory category;

    @OneToMany(mappedBy = "type")
    private Set<PlantSpecies> species = new LinkedHashSet<>();

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

    public void setCategory(PlantCategory category) {
        this.category = category;
    }

    public Set<PlantSpecies> getSpecies() {
        return species;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantType plantType = (PlantType) o;
        if (id == null || plantType.id == null) return false;
        return Objects.equals(id, plantType.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
