# Ultras — Football Rating & Review Platform

> *Rate matches. Write reviews. Follow the game the way you feel it.*

Ultras is a web-based football rating and reviewing platform built with Java, Spring Boot and Vaadin. It allows registered users to rate completed fixtures and individual team performances using a half-star system, write match reviews, explore league statistics and maintain a personal diary of their football viewing activity. Match data is sourced from the Sportmonks v3 API and synchronised to a PostgreSQL database on application startup.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [API Integration](#api-integration)
- [Known Limitations](#known-limitations)

---

## Overview

Ultras was developed as a final year university project at UWE Bristol. The motivation came from primary research showing that modern football discourse is increasingly dominated by statistical analysis — xG, possession percentages, pass accuracy — at the expense of the fan's subjective experience. Ultras addresses this by providing a space where personal evaluation and emotional engagement are the primary instruments of assessment, drawing inspiration from Letterboxd's rating and reviewing model and SofaScore's interface conventions.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vaadin Flow (Java) |
| Backend | Spring Boot 3 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + BCrypt |
| External API | Sportmonks v3 Football API |
| Build Tool | Maven |

---

## Features

- **Half-star rating system** — rate fixtures and individual team performances from 0.5 to 5 in 0.5 increments
- **Match reviews** — write and edit a personal review for any fixture
- **Match details** — view key events (goals, cards, substitutions), head-to-head record with ratings, and team lineups sourced from Sportmonks
- **Matches feed** — browse fixtures grouped by league, filtered by date
- **Search** — search for fixtures by team name, supports two-team queries
- **League pages** — view average league rating, top 3 rated teams and highest rated head-to-head fixture
- **Personal diary** — chronological log of all rated fixtures
- **Profile** — team of the season and match of the season derived from your ratings
- **Settings** — update username, password, favourite team or delete account
- **Guest browsing** — matches, fixtures and leagues are accessible without signing in

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- A Sportmonks v3 API token (free tier is sufficient)

---

## Setup and Installation

**1. Clone the repository**

```bash
git clone https://github.com/yourusername/ultras.git
cd ultras
```

**2. Create the database**

Open your PostgreSQL client and run:

```sql
CREATE DATABASE "ZainabMB";
```

**3. Run the schema script**

```bash
psql -U your_username -d ZainabMB -f db.sql
```

This creates the `ultras` schema and all required tables with constraints.

**4. Configure application properties**

Copy the example properties file and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ZainabMB
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.properties.hibernate.default_schema=ultras

# Sportmonks API
sportmonks.api.base-url=https://api.sportmonks.com/v3/football
sportmonks.api.token=your_sportmonks_api_token
```

> ⚠️ Never commit `application.properties` with real credentials. It is included in `.gitignore`.

---

## Running the Application

```bash
mvn spring-boot:run
```

On startup the application will:
1. Connect to the PostgreSQL database
2. Run `SyncService` via `@PostConstruct` — fetching leagues, teams and completed fixtures from Sportmonks for the 2024/2025 season
3. Skip any records already present in the database (`existsById()` check prevents duplicates)
4. Make the application available at `http://localhost:8080`

> The first startup sync may take up to 60 seconds depending on API response times.

---

## Database Schema

All tables are created under the `ultras` schema inside the `ZainabMB` database.

| Table | Description |
|---|---|
| `user` | Registered user accounts |
| `fixture` | Completed match fixtures synced from Sportmonks |
| `team` | Team records synced from Sportmonks |
| `league` | League records synced from Sportmonks |
| `rating` | User ratings for fixtures and teams (0.5–5, UNIQUE per user/fixture/team) |
| `review` | User written reviews for fixtures (UNIQUE per user/fixture) |

Key constraints:
- `rating.score` — `CHECK (score >= 0.5 AND score <= 5)`
- `rating` — `UNIQUE(user_id, fixture_id, team_id)`
- `review` — `UNIQUE(user_id, fixture_id)`
- Passwords stored as BCrypt hashes — never plain text

---

## Project Structure

```
src/main/java/com/example/
├── config/
│   └── SecurityConfig.java         # Spring Security route protection
├── model/
│   ├── User.java
│   ├── Fixture.java
│   ├── Team.java
│   ├── League.java
│   ├── Rating.java
│   ├── Review.java
│   └── dto/
│       └── FixtureResponse.java    # Data transfer object for fixture display
├── repository/                     # Spring Data JPA interfaces
├── service/
│   ├── UserService.java
│   ├── FixtureService.java
│   ├── RatingService.java
│   ├── ReviewService.java
│   ├── LeagueService.java
│   ├── SportmonksService.java      # All Sportmonks API calls isolated here
│   └── SyncService.java            # @PostConstruct startup sync
└── views/
    ├── HomeView.java
    ├── SignInView.java
    ├── SignUpView.java
    ├── MatchesView.java
    ├── FixtureDetailView.java
    ├── LeagueView.java
    ├── LeagueDetailView.java
    ├── ProfileView.java
    ├── SettingsView.java
    ├── DiaryView.java
    ├── UserReviewsView.java
    └── components/
        ├── SearchComponent.java
        ├── MatchDetailsComponent.java
        └── ReviewDialog.java
```

---

## API Integration

Ultras integrates with the [Sportmonks v3 Football API](https://docs.sportmonks.com/football). All API calls are isolated within `SportmonksService` — no other class constructs Sportmonks URLs directly. This means switching to a different data provider requires changes to only one class.

**Endpoints used:**

| Method | Endpoint | Used for |
|---|---|---|
| GET | `/leagues?include=seasons` | Fetch all leagues with seasons |
| GET | `/seasons/{id}?include=fixtures.participants;fixtures.scores` | Sync fixtures for a season |
| GET | `/teams/seasons/{id}` | Sync teams for a season |
| GET | `/fixtures/{id}?include=participants;scores;state;league;events.type;events.player;lineups.player;lineups.position;formations` | Full fixture detail for match page |

---

## Known Limitations

- **4 leagues only** — constrained by Sportmonks free tier rate limits
- **One review per fixture** — by design, to prevent spam
- **Web only** — no native mobile app; responsive design is limited
- **Local deployment only** — not configured for cloud or multi-instance deployment
- **Synchronous API calls** — fixture detail page blocks until Sportmonks responds

---

## Author

Zainab MB — UWE Bristol, BSc Software Engineering, 2025–26
