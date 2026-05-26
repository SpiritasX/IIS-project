package com.example.iis.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends Account {
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Process> processes = new ArrayList<>();

    public Customer() {
        super();
    }

    public Customer(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }

    public Customer(String username, String password, String firstName, String lastName, String email, String address) {
        super(username, password, firstName, lastName, email, address);
    }

    public List<Report> getReports() {
        return reports;
    }

    public List<Process> getProcesses() {
        return processes;
    }
}
