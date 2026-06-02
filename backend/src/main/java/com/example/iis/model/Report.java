package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String description;

    @Column(nullable = false)
    private Date createdAt;

    private Date resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Plant plant;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportStatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    public Report() {
    }

    public Report(Customer customer, Offer offer, String description) {
        this.customer = customer;
        this.offer = offer;
        this.description = description;
        this.createdAt = new Date(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void resolve() {
        resolvedAt = new Date(System.currentTimeMillis());
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public List<ReportStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void addStatusHistory(ReportStatusHistory statusHistory) {
        this.statusHistory.add(statusHistory);
        statusHistory.setReport(this);
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
        if (feedback != null) {
            feedback.setReport(this);
        }
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
        Report report = (Report) o;
        if (id == null || report.id == null) return false;
        return Objects.equals(id, report.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
