# Movie Ticket Reservation Design

## Goal

Build a load-test friendly movie ticket reservation flow on top of the existing Spring Boot EAV service. Users can browse seeded movies and showtimes, request a ticket quantity, and receive allocated seats when capacity remains.

## Scope

The flow has no login and no user-selected seats. Seat allocation is automatic and deterministic by availability. Tickets are limited by seeded seats in each cinema room. A seat belongs to a room, a showtime belongs to a room and movie, and every successful ticket is allocated to a concrete seat.

## Data Model

The app seeds EAV entity types and attributes for:

- `movie`: title, genre, rating, duration minutes, synopsis, poster URL.
- `cinema_room`: name, row count, seats per row.
- `seat`: code, row label, seat number, active flag.
- `showtime`: movie id, room id, starts at, ends at, price, total capacity, available capacity.
- `ticket_reservation`: showtime id, quantity, allocated seats, status, reserved at.

Relationships document the cinema graph: room to seats, movie to showtimes, showtime to room, reservation to showtime, and reservation to seats.

## Backend Flow

`GET /api/v1/cinema/movies` returns movies with showtime cards and availability. `POST /api/v1/cinema/reservations` starts the Camunda process `movie-ticket-reservation` synchronously. The Camunda worker validates quantity and showtime id, locks the showtime capacity row, selects available seats not already allocated for that showtime, creates the reservation, records allocated seats, and decrements available capacity.

Insufficient capacity returns a business error in the existing `result.error` shape used by `WorkflowServiceImpl`.

## Camunda

The BPMN file is stored at `src/main/resources/camunda/movie-ticket-reservation.bpmn`. A startup component deploys it automatically when Camunda is reachable. If Camunda is not reachable, startup continues and logs the deployment failure so local database/API tests are not blocked.

## Frontend

The static frontend is a plain `index.html` with inline CSS and JavaScript. It shows a grid of seeded movies, showtime buttons, a selected showtime panel, ticket quantity input, reserve button, and result/error messages. It calls the cinema APIs directly.

## Docker

A root `docker-compose.yml` provides Postgres and a lightweight Camunda 8 stack for local use. The app itself still runs from Maven or an IDE.

## Testing

Tests cover seed idempotency, movie listing, successful reservation, over-capacity failure, and capacity decrement. BPMN deployment is isolated from tests through configuration.
