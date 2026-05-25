package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
//    private String imageUrl;
    private String status;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RelocationHistory> relocationHistory = new ArrayList<>();
    @ManyToOne(fetch = FetchType.EAGER)
    private PlantVariety variety;

    public Plant() {
    }

    public Plant(String name, String description, String status, PlantVariety variety) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.variety = variety;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RelocationHistory> getRelocationHistory() {
        return relocationHistory;
    }

    public void addRelocationHistory(RelocationHistory relocationHistory) {
        this.relocationHistory.add(relocationHistory);
    }

    public PlantVariety getVariety() {
        return variety;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Plant plant)) return false;
        return Objects.equals(id, plant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
