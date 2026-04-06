package com.example.model;
// model/Fixture.java
@Entity
@Table(name = "fixture", schema = "ultras")
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fixtureId;

    @Column(name = "home_team_id")
    private Long homeTeamId;

    @Column(name = "away_team_id")
    private Long awayTeamId;

    @Column(name = "result")
    private Double result;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    // getters and setters
}
