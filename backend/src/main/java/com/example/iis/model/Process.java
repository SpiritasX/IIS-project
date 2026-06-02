package com.example.iis.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "processes")
public class Process {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date startTime;

    private Date endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Customer customer;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phase> phases = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "process_admin_editors",
            joinColumns = @JoinColumn(name = "process_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id")
    )
    private Set<Admin> adminEditors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "process_botanist_editors",
            joinColumns = @JoinColumn(name = "process_id"),
            inverseJoinColumns = @JoinColumn(name = "botanist_id")
    )
    private Set<Botanist> botanistEditors = new LinkedHashSet<>();

    public Process() {
    }

    public Process(Customer customer) {
        this.customer = customer;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public void addPhase(Phase phase) {
        phases.add(phase);
        phase.setProcess(this);
    }

    public void removePhase(Phase phase) {
        phases.remove(phase);
        phase.setProcess(null);
    }

    public Set<Admin> getAdminEditors() {
        return adminEditors;
    }

    public void addAdminEditor(Admin admin) {
        adminEditors.add(admin);
    }

    public void removeAdminEditor(Admin admin) {
        adminEditors.remove(admin);
    }

    public Set<Botanist> getBotanistEditors() {
        return botanistEditors;
    }

    public void addBotanistEditor(Botanist botanist) {
        botanistEditors.add(botanist);
    }

    public void removeBotanistEditor(Botanist botanist) {
        botanistEditors.remove(botanist);
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
        Process process = (Process) o;
        if (id == null || process.id == null) return false;
        return Objects.equals(id, process.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
