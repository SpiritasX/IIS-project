package com.example.iis.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "plant_condition_log")
public class PlantConditionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id")
    private Plant plant;

    private Integer conditionState;
    private String conditionDescription;
    private String color;
    private Double height;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(nullable = false)
    private String changedBy;

    protected PlantConditionLog() {}

    public PlantConditionLog(Plant plant, Integer conditionState, String conditionDescription,
                              String color, Double height, String changedBy) {
        this.plant = plant;
        this.conditionState = conditionState;
        this.conditionDescription = conditionDescription;
        this.color = color;
        this.height = height;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Plant getPlant() { return plant; }
    public Integer getConditionState() { return conditionState; }
    public String getConditionDescription() { return conditionDescription; }
    public String getColor() { return color; }
    public Double getHeight() { return height; }
    public Instant getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
}
