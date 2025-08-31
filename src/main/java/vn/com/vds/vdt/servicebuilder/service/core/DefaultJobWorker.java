package vn.com.vds.vdt.servicebuilder.service.core;

import io.camunda.client.CamundaClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("all")
@RequiredArgsConstructor
@Slf4j
public class DefaultJobWorker {
    @Value("${spring.application.name}")
    private String serviceName;

    private final CamundaClient camundaClient;

    @PostConstruct
    public void registerWorker() {
        camundaClient.newWorker()
                .jobType(serviceName)
                .handler((client, job) -> {
                    client.newCompleteCommand(job.getKey())
                            .variables(job.getVariablesAsMap())
                            .send()
                            .join();
                })
                .open();
        log.info("Registered job worker for type '{}'", serviceName);
    }
}
