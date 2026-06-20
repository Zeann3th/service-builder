package vn.com.vds.vdt.servicebuilder.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.MovieResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ReservationResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ShowtimeResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;
import vn.com.vds.vdt.servicebuilder.service.cinema.CinemaReservationService;
import vn.com.vds.vdt.servicebuilder.service.core.WorkflowService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaController.class)
class CinemaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CinemaReservationService cinemaReservationService;

    @MockitoBean
    private WorkflowService workflowService;

    @Test
    void getMoviesReturnsSeededMovieGridData() throws Exception {
        when(cinemaReservationService.getMovies()).thenReturn(List.of(MovieResponse.builder()
                .id(1L)
                .title("Dune: Part Two")
                .genre("Sci-Fi")
                .rating("PG-13")
                .durationMinutes(166)
                .showtimes(List.of(ShowtimeResponse.builder()
                        .id(10L)
                        .roomName("Room A")
                        .startsAt(LocalDateTime.parse("2026-06-20T20:00:00"))
                        .availableCapacity(40)
                        .build()))
                .build()));

        mockMvc.perform(get("/api/v1/cinema/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.data[0].showtimes[0].availableCapacity").value(40));
    }

    @Test
    void reserveStartsMovieTicketWorkflowSynchronously() throws Exception {
        when(workflowService.executeSync(eq("movie-ticket-reservation"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(ReservationResponse.builder()
                        .reservationId(99L)
                        .showtimeId(10L)
                        .quantity(2)
                        .allocatedSeats(List.of("A1", "A2"))
                        .remainingCapacity(38)
                        .status("RESERVED")
                        .build());

        mockMvc.perform(post("/api/v1/cinema/reservations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(Map.of("showtimeId", 10L, "quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationId").value(99))
                .andExpect(jsonPath("$.data.allocatedSeats[0]").value("A1"));

        ArgumentCaptor<TriggerWorkflowRequest> captor = ArgumentCaptor.forClass(TriggerWorkflowRequest.class);
        verify(workflowService).executeSync(eq("movie-ticket-reservation"), captor.capture());
        assertThat(captor.getValue().getVariables()).containsEntry("showtimeId", 10L);
        assertThat(captor.getValue().getVariables()).containsEntry("quantity", 2);
    }
}
