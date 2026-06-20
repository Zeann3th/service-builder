package vn.com.vds.vdt.servicebuilder.service.cinema;

import io.camunda.client.CamundaClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ReservationResponse;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.cinema.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CinemaReservationWorker {
    public static final String JOB_TYPE = "cinema-reserve-tickets";

    private final CamundaClient camundaClient;
    private final CinemaReservationService cinemaReservationService;

    @PostConstruct
    public void registerWorker() {
        camundaClient.newWorker()
                .jobType(JOB_TYPE)
                .handler((client, job) -> {
                    Map<String, Object> variables = job.getVariablesAsMap();
                    try {
                        Long showtimeId = asLong(variables.get("showtimeId"));
                        Integer quantity = asInteger(variables.get("quantity"));
                        ReservationResponse response = cinemaReservationService.reserve(showtimeId, quantity);
                        client.newCompleteCommand(job.getKey())
                                .variables(Map.of("result", response))
                                .send()
                                .join();
                    } catch (CinemaReservationException e) {
                        client.newCompleteCommand(job.getKey())
                                .variables(Map.of("result", Map.of(
                                        "error", Map.of(
                                                "code", e.getCode(),
                                                "message", e.getMessage()
                                        )
                                )))
                                .send()
                                .join();
                    } catch (Exception e) {
                        log.error("Cinema reservation worker failed", e);
                        client.newCompleteCommand(job.getKey())
                                .variables(Map.of("result", Map.of(
                                        "error", Map.of(
                                                "code", CinemaReservationService.ERR_BAD_REQUEST,
                                                "message", e.getMessage()
                                        )
                                )))
                                .send()
                                .join();
                    }
                })
                .open();
        log.info("Registered Camunda job worker for type '{}'", JOB_TYPE);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }
}
