# Movie Ticket Reservation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an EAV-backed movie ticket reservation demo with seeded cinema data, Camunda BPMN execution, automatic seat allocation, static frontend, Docker compose, and tests.

**Architecture:** Keep generic EAV CRUD intact and add a focused `cinema` package for query and reservation use cases. Store cinema records in existing EAV tables while using transactional service code for capacity and seat allocation. Deploy BPMN automatically at startup and execute reservations through a Camunda job worker.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, Camunda 8 client, PostgreSQL, H2 for tests, plain static HTML/CSS/JS.

---

### Task 1: Test Dependencies And DTO Contracts

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/controller/dto/cinema/CinemaDtos.java`
- Test: `src/test/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaReservationServiceTest.java`

- [ ] Add H2 test dependency and define request/response DTOs.
- [ ] Write failing service tests for listing movies and reservations.
- [ ] Run `.\mvnw.cmd -Dtest=CinemaReservationServiceTest test` and confirm compilation/test failure because service classes do not exist.

### Task 2: EAV Seed Service

**Files:**
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaSeedService.java`
- Modify: repositories where query helpers are needed.
- Test: `src/test/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaSeedServiceTest.java`

- [ ] Write failing tests for idempotent entity type and sample data seeding.
- [ ] Implement seed service using existing EAV entities and repositories.
- [ ] Run seed tests and reservation tests.

### Task 3: Reservation Service

**Files:**
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaReservationService.java`
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaReservationException.java`
- Modify: repositories for lock/query helpers.
- Test: `src/test/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaReservationServiceTest.java`

- [ ] Implement movie listing from seeded EAV data.
- [ ] Implement transactional reservation with automatic seat allocation.
- [ ] Return allocated seat codes and updated capacity.
- [ ] Return a business exception when capacity is insufficient.
- [ ] Run `.\mvnw.cmd -Dtest=CinemaSeedServiceTest,CinemaReservationServiceTest test`.

### Task 4: Camunda BPMN And Worker

**Files:**
- Create: `src/main/resources/camunda/movie-ticket-reservation.bpmn`
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/service/cinema/CinemaReservationWorker.java`
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/service/core/CamundaDeploymentService.java`
- Modify: `src/main/resources/application.yaml`
- Test: extend `CinemaReservationServiceTest` for worker result shape where practical.

- [ ] Add BPMN with service tasks for validate/reserve and success/failure paths.
- [ ] Add startup deployment guarded by configuration and reachable Camunda.
- [ ] Add worker that completes jobs with `result` or `result.error`.
- [ ] Run focused tests.

### Task 5: REST API And Static Frontend

**Files:**
- Create: `src/main/java/vn/com/vds/vdt/servicebuilder/controller/CinemaController.java`
- Replace: `src/main/resources/static/index.html`
- Modify/Create: `src/main/resources/static/app.css`, `src/main/resources/static/app.js`
- Test: `src/test/java/vn/com/vds/vdt/servicebuilder/controller/CinemaControllerTest.java`

- [ ] Write controller test for movie listing and reservation request.
- [ ] Implement `GET /api/v1/cinema/movies`.
- [ ] Implement `POST /api/v1/cinema/reservations`.
- [ ] Build the static movie grid and ticket count form.
- [ ] Run controller tests.

### Task 6: Docker And Final Verification

**Files:**
- Create: `docker-compose.yml`
- Modify: `src/main/resources/camunda/docker-compose.yml` if needed.

- [ ] Add local Postgres and Camunda compose.
- [ ] Run `.\mvnw.cmd test`.
- [ ] Run `git status --short`.
- [ ] Commit and push branch `thesis`.
