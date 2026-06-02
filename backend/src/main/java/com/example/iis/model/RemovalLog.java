package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class RemovalLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String reason;
    @Column(nullable = false)
    private Date timestamp;
    private Long quantity;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Botanist botanist;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    private Plant plant;

    public RemovalLog() {
    }

    public RemovalLog(String reason, Long quantity, Botanist botanist, Plant plant) {
        this.reason = reason;
        this.timestamp = new Date(System.currentTimeMillis());
        this.quantity = quantity;
        this.botanist = botanist;
        this.plant = plant;
    }

    public Long getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Long getQuantity() {
        return quantity;
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
        RemovalLog that = (RemovalLog) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
