package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Botanist extends Account {
    @ManyToMany(mappedBy = "botanistEditors")
    private Set<Process> editedProcesses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "botanist")
    private List<HealthLog> healthLogs = new ArrayList<>();

    @OneToMany(mappedBy = "botanist")
    private List<RemovalLog> removalLogs = new ArrayList<>();

    public Botanist() {
        super();
    }

    public Botanist(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }

    public Set<Process> getEditedProcesses() {
        return editedProcesses;
    }

    public List<HealthLog> getHealthLogs() {
        return healthLogs;
    }

    public List<RemovalLog> getRemovalLogs() {
        return removalLogs;
    }
}
