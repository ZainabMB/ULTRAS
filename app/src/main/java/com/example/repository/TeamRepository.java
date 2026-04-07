package com.example.repository;

import com.example.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // JpaRepository gives us save(), findById(), existsById(), findAll() for free
    // team_id IS the Sportmonks ID so no extra lookup methods needed
}