package com.example.service;

import com.example.model.Fixture;
import com.example.model.League;
import com.example.model.Team;
import com.example.model.dto.FixtureResponse;
import com.example.repository.LeagueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    public List<League> getAllLeagues() {
        return leagueRepository.findAllByOrderByLeagueNameAsc();
    }
    public League getLeagueById(Long leagueId) {
        return leagueRepository.findById(leagueId).orElseThrow();
    }

}
