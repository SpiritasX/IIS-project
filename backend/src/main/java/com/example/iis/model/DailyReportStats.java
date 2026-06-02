package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "daily_report_stats")
public class DailyReportStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Date date;

    private Long totalReports;
    private Long totalOrders;
    private Double reportRatio;
    private Double averageResponseTime;
    private Long resolvedReports;
    private Long unresolvedReports;
    private String topProblematicPlants;
    private String topProblemType;

    public DailyReportStats() {
    }

    public DailyReportStats(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(Long totalReports) {
        this.totalReports = totalReports;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Double getReportRatio() {
        return reportRatio;
    }

    public void setReportRatio(Double reportRatio) {
        this.reportRatio = reportRatio;
    }

    public Double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Long getResolvedReports() {
        return resolvedReports;
    }

    public void setResolvedReports(Long resolvedReports) {
        this.resolvedReports = resolvedReports;
    }

    public Long getUnresolvedReports() {
        return unresolvedReports;
    }

    public void setUnresolvedReports(Long unresolvedReports) {
        this.unresolvedReports = unresolvedReports;
    }

    public String getTopProblematicPlants() {
        return topProblematicPlants;
    }

    public void setTopProblematicPlants(String topProblematicPlants) {
        this.topProblematicPlants = topProblematicPlants;
    }

    public String getTopProblemType() {
        return topProblemType;
    }

    public void setTopProblemType(String topProblemType) {
        this.topProblemType = topProblemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyReportStats that = (DailyReportStats) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
