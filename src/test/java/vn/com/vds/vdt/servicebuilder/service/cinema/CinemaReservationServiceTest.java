package vn.com.vds.vdt.servicebuilder.service.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ReservationResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({CinemaSeedService.class, CinemaReservationService.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.cinema.seed.enabled=true"
})
class CinemaReservationServiceTest {
    @Autowired
    private CinemaSeedService seedService;

    @Autowired
    private CinemaReservationService reservationService;

    @BeforeEach
    void setUp() {
        seedService.seed();
    }

    @Test
    void listsSeededMoviesWithShowtimesAndCapacity() {
        var movies = reservationService.getMovies();

        assertThat(movies).isNotEmpty();
        assertThat(movies.getFirst().getShowtimes()).isNotEmpty();
        assertThat(movies.getFirst().getShowtimes().getFirst().getAvailableCapacity()).isPositive();
    }

    @Test
    void reserveTicketsAllocatesConcreteSeatsAndDecrementsCapacity() {
        var showtime = reservationService.getMovies().getFirst().getShowtimes().getFirst();

        ReservationResponse response = reservationService.reserve(showtime.getId(), 2);

        assertThat(response.getReservationId()).isNotNull();
        assertThat(response.getAllocatedSeats()).hasSize(2);
        assertThat(response.getRemainingCapacity()).isEqualTo(showtime.getAvailableCapacity() - 2);
    }

    @Test
    void reserveTicketsRejectsRequestWhenCapacityIsInsufficient() {
        var showtime = reservationService.getMovies().getFirst().getShowtimes().getFirst();

        assertThatThrownBy(() -> reservationService.reserve(showtime.getId(), showtime.getAvailableCapacity() + 1))
                .isInstanceOf(CinemaReservationException.class)
                .hasMessageContaining("Not enough seats");
    }
}
