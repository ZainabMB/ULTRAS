package com.example.repository;

import com.example.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    // Used for the matches feed screen — get all fixtures on a given date
    List<Fixture> findByDate(LocalDate date);
}