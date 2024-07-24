package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String language;

    @ManyToOne
    @JoinColumn(name = "administrator_id")
    private Administrateur administrator;


    @ManyToMany
    @JoinTable(name = "candidate_test", joinColumns = @JoinColumn(name = "test_id"), inverseJoinColumns = @JoinColumn(name = "candidate_id"))
    private List<Condidats> candidates;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Administrateur getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrateur administrator) {
        this.administrator = administrator;
    }


    public List<Condidats> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Condidats> candidates) {
        this.candidates = candidates;
    }
}
