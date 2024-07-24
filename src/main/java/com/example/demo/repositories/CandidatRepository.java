package com.example.demo.repositories;

import com.example.demo.model.Condidats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatRepository extends JpaRepository<Condidats, Long> {
}
