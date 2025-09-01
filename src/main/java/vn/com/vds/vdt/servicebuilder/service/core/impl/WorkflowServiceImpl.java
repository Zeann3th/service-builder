package vn.com.vds.vdt.servicebuilder.service.core.impl;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;
import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;
import vn.com.vds.vdt.servicebuilder.exception.CommandExceptionBuilder;
import vn.com.vds.vdt.servicebuilder.service.core.WorkflowService;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public class WorkflowServiceImpl implements WorkflowService {

    private final CamundaClient camundaClient;

    @Override
    public void execute(String workflowName, TriggerWorkflowRequest request) {
        try {
            int version = Objects.isNull(request.getVersion()) ? -1 : request.getVersion();

            var command = camundaClient.newCreateInstanceCommand()
                    .bpmnProcessId(workflowName)
                    .version(version)
                    .variables(request.getVariables());

            var instance = command.send().join();

            log.info("Started workflow '{}' with definitionKey={} and instanceKey={}",
                    workflowName, instance.getProcessDefinitionKey(), instance.getProcessInstanceKey());

        } catch (Exception e) {
            log.error("Failed to start workflow '{}': {}", workflowName, e.getMessage(), e);
            throw CommandExceptionBuilder.exception(
                    ErrorCodes.QS00004,
                    "Failed to start workflow due to non-existent workflow or invalid version"
            );
        }
    }

    @Override
    public Map<String, Object> executeSync(String workflowName, TriggerWorkflowRequest request) {
        try {
            int version = Objects.isNull(request.getVersion()) ? -1 : request.getVersion();
            ProcessInstanceResult pi = camundaClient.newCreateInstanceCommand()
                    .bpmnProcessId(workflowName)
                    .version(version)
                    .variables(request.getVariables())
                    .withResult()
                    .send()
                    .join();

            return pi.getVariablesAsMap();
        } catch (Exception e) {
            log.error("Failed to start workflow '{}': {}", workflowName, e.getMessage(), e);
            throw CommandExceptionBuilder.exception(
                    ErrorCodes.QS00004,
                    "Failed to start workflow due to non-existent workflow or invalid version"
            );
        }
    }
}
