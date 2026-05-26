package com.example.iis.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "phase_type")
public class PhaseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "type")
    private Set<Phase> phases = new LinkedHashSet<>();

    public PhaseType() {
    }

    public PhaseType(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Phase> getPhases() {
        return phases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhaseType phaseType = (PhaseType) o;
        if (id == null || phaseType.id == null) return false;
        return Objects.equals(id, phaseType.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
