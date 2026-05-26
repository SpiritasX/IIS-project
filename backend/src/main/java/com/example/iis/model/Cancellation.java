package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class Cancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    @Column(nullable = false)
    private Date createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phase_id", unique = true)
    private Phase phase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CancellationReason cancellationReason;

    public Cancellation() {
    }

    public Cancellation(Phase phase, CancellationReason cancellationReason, String reason) {
        this.phase = phase;
        this.cancellationReason = cancellationReason;
        this.reason = reason;
        this.createdAt = new Date(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancellation that = (Cancellation) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
