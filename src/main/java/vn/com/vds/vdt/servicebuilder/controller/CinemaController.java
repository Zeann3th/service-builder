package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.MovieResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ReservationRequest;
import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;
import vn.com.vds.vdt.servicebuilder.service.cinema.CinemaReservationService;
import vn.com.vds.vdt.servicebuilder.service.core.WorkflowService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cinema")
@RequiredArgsConstructor
@ResponseWrapper
public class CinemaController {
    private static final String WORKFLOW_ID = "movie-ticket-reservation";

    private final CinemaReservationService cinemaReservationService;
    private final WorkflowService workflowService;

    @GetMapping("/movies")
    public List<MovieResponse> getMovies() {
        return cinemaReservationService.getMovies();
    }

    @PostMapping("/reservations")
    public Object reserve(@RequestBody ReservationRequest request) {
        return workflowService.executeSync(WORKFLOW_ID, TriggerWorkflowRequest.builder()
                .resultKey("result")
                .errorKey("error")
                .variables(Map.of(
                        "showtimeId", request.getShowtimeId(),
                        "quantity", request.getQuantity()
                ))
                .build());
    }
}
