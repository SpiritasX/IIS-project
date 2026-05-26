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
    private String propagationMethod;
    private String status;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelocationHistory> relocationHistory = new ArrayList<>();

    @OneToMany(mappedBy = "plant")
    private List<PlantPrice> prices = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthLog> healthLogs = new ArrayList<>();

    @OneToMany(mappedBy = "plant")
    private List<Report> reports = new ArrayList<>();

    @OneToOne(mappedBy = "plant")
    private RemovalLog removalLog;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private PlantVariety variety;

    public Plant() {
    }

    public Plant(String name, String description, String status, PlantVariety variety) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.variety = variety;
    }

    public Plant(String name, String description, String propagationMethod, String status, PlantVariety variety) {
        this(name, description, status, variety);
        this.propagationMethod = propagationMethod;
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

    public String getPropagationMethod() {
        return propagationMethod;
    }

    public void setPropagationMethod(String propagationMethod) {
        this.propagationMethod = propagationMethod;
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
        relocationHistory.setPlant(this);
    }

    public void removeRelocationHistory(RelocationHistory relocationHistory) {
        this.relocationHistory.remove(relocationHistory);
        relocationHistory.setPlant(null);
    }

    public List<PlantPrice> getPrices() {
        return prices;
    }

    public List<HealthLog> getHealthLogs() {
        return healthLogs;
    }

    public List<Report> getReports() {
        return reports;
    }

    public RemovalLog getRemovalLog() {
        return removalLog;
    }

    public PlantVariety getVariety() {
        return variety;
    }

    public void setVariety(PlantVariety variety) {
        this.variety = variety;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plant plant = (Plant) o;
        if (id == null || plant.id == null) return false;
        return Objects.equals(id, plant.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
