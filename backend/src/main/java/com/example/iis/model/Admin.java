package com.example.iis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class Admin extends Account {
    @ManyToMany(mappedBy = "adminEditors")
    private Set<Process> editedProcesses = new LinkedHashSet<>();

    public Admin() {
        super();
    }
    
    public Admin(String username, String password, String firstName, String lastName, String email) {
        super(username, password, firstName, lastName, email);
    }

    public Set<Process> getEditedProcesses() {
        return editedProcesses;
    }
}
