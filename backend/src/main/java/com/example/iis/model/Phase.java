package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class Phase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date startTime;

    private Date endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Process process;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PhaseType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Offer offer;

    @OneToOne(mappedBy = "phase", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cancellation cancellation;

    public Phase() {
    }

    public Phase(Process process, PhaseType type, Offer offer) {
        this.process = process;
        this.type = type;
        this.offer = offer;
        this.startTime = new Date(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void finish() {
        endTime = new Date(System.currentTimeMillis());
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public PhaseType getType() {
        return type;
    }

    public void setType(PhaseType type) {
        this.type = type;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Cancellation getCancellation() {
        return cancellation;
    }

    public void setCancellation(Cancellation cancellation) {
        this.cancellation = cancellation;
        if (cancellation != null) {
            cancellation.setPhase(this);
        }
    }

    @PrePersist
    void prePersist() {
        if (startTime == null) {
            startTime = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phase phase = (Phase) o;
        if (id == null || phase.id == null) return false;
        return Objects.equals(id, phase.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
