package com.example.iis.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "plant_relocation_log")
public class PlantRelocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_sector_id")
    private Sector fromSector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_sector_id")
    private Sector toSector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelocationState state;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    @Column(nullable = false)
    private String initiatedBy;

    protected PlantRelocationLog() {}

    public PlantRelocationLog(Plant plant, Sector fromSector, Sector toSector, String initiatedBy) {
        this.plant = plant;
        this.fromSector = fromSector;
        this.toSector = toSector;
        this.initiatedBy = initiatedBy;
        this.state = RelocationState.WAITING;
        this.startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Plant getPlant() { return plant; }
    public Sector getFromSector() { return fromSector; }
    public Sector getToSector() { return toSector; }
    public RelocationState getState() { return state; }
    public void setState(RelocationState state) { this.state = state; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getInitiatedBy() { return initiatedBy; }
}
