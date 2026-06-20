package vn.com.vds.vdt.servicebuilder.service.core;

import io.camunda.client.CamundaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.camunda.deployment", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CamundaDeploymentService {
    private static final String MOVIE_TICKET_BPMN = "camunda/movie-ticket-reservation.bpmn";

    private final CamundaClient camundaClient;

    @EventListener(ApplicationReadyEvent.class)
    public void deployResources() {
        try {
            camundaClient.newDeployResourceCommand()
                    .addResourceFromClasspath(MOVIE_TICKET_BPMN)
                    .send()
                    .join(5, TimeUnit.SECONDS);
            log.info("Deployed Camunda BPMN '{}'", MOVIE_TICKET_BPMN);
        } catch (Exception e) {
            log.warn("Skipped Camunda BPMN deployment for '{}': {}", MOVIE_TICKET_BPMN, e.getMessage());
        }
    }
}
