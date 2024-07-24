package com.example.demo.repositories;

import com.example.demo.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NiveauRepository extends JpaRepository<Level, Long> {
    List<Level> findByThemeId(Long themeId);
}
