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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthLog healthLog)) return false;
        return Objects.equals(id, healthLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
