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
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RemovalLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
