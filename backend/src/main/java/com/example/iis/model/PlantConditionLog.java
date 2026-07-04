package com.example.iis.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "plant_condition_log")
public class PlantConditionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @Column(name = "original_plant_id")
    private Long originalPlantId;

    private Integer conditionState;
    private String conditionDescription;
    private String color;
    private Double height;
    private String lifecycleStage;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(nullable = false)
    private String changedBy;

    protected PlantConditionLog() {}

    public PlantConditionLog(Plant plant, Integer conditionState, String conditionDescription,
                              String color, Double height, String lifecycleStage, String changedBy) {
        this.plant = plant;
        this.originalPlantId = plant.getId();
        this.conditionState = conditionState;
        this.conditionDescription = conditionDescription;
        this.color = color;
        this.height = height;
        this.lifecycleStage = lifecycleStage;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Plant getPlant() { return plant; }
    public void setPlant(Plant plant) { this.plant = plant; }
    public Long getOriginalPlantId() { return originalPlantId; }
    public Integer getConditionState() { return conditionState; }
    public String getConditionDescription() { return conditionDescription; }
    public String getColor() { return color; }
    public Double getHeight() { return height; }
    public String getLifecycleStage() { return lifecycleStage; }
    public Instant getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
}
