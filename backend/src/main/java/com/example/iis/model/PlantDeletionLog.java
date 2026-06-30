package com.example.iis.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "plant_deletion_log")
public class PlantDeletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plantName;

    @Column(nullable = false)
    private String varietyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionReason reason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date deletedAt;

    @Column(nullable = false)
    private String deletedBy;

    protected PlantDeletionLog() {}

    public PlantDeletionLog(String plantName, String varietyName, DeletionReason reason, String deletedBy) {
        this.plantName = plantName;
        this.varietyName = varietyName;
        this.reason = reason;
        this.deletedBy = deletedBy;
        this.deletedAt = new Date();
    }

    public Long getId() { return id; }
    public String getPlantName() { return plantName; }
    public String getVarietyName() { return varietyName; }
    public DeletionReason getReason() { return reason; }
    public Date getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}
