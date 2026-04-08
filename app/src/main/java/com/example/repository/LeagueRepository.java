package com.example.repository;

import com.example.model.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueRepository extends JpaRepository<League, Long> {
    List<League> findAllByOrderByLeagueNameAsc();
}
