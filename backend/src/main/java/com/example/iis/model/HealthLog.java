package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class HealthLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lifeStage;
    private String healthGrade;
    private Date timestamp;
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Botanist botanist;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Plant plant;

    public HealthLog() {
    }

    public HealthLog(String lifeStage, String healthGrade, String description, Botanist botanist, Plant plant) {
        this.lifeStage = lifeStage;
        this.healthGrade = healthGrade;
        this.timestamp = new Date(System.currentTimeMillis());
        this.description = description;
        this.botanist = botanist;
        this.plant = plant;
    }

    public Long getId() {
        return id;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public String getHealthGrade() {
        return healthGrade;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Botanist getBotanist() {
        return botanist;
    }

    public Plant getPlant() {
        return plant;
    }

    @PrePersist
    void prePersist() {
        if (timestamp == null) {
            timestamp = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthLog healthLog = (HealthLog) o;
        if (id == null || healthLog.id == null) return false;
        return Objects.equals(id, healthLog.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
