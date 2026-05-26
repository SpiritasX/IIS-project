package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
public class ReportStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Date changedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Report report;

    public ReportStatusHistory() {
    }

    public ReportStatusHistory(Report report, String status) {
        this.report = report;
        this.status = status;
        this.changedAt = new Date(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getChangedAt() {
        return changedAt;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @PrePersist
    void prePersist() {
        if (changedAt == null) {
            changedAt = new Date(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportStatusHistory that = (ReportStatusHistory) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
