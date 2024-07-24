package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Administrateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @OneToMany(mappedBy = "administrator")
    private List<Test> tests;

    // getters and setters

    // Getters
    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public List<Test> getTests() {
        return tests;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}
