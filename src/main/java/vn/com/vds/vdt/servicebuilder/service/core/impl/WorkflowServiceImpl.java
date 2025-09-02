package vn.com.vds.vdt.servicebuilder.service.core.impl;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.ClientException;
import io.camunda.client.api.command.ClientStatusException;
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

        } catch (ClientStatusException e) {
            log.error("Failed to start workflow '{}': {}", workflowName, e.getMessage(), e);
            throw CommandExceptionBuilder.exception(
                    ErrorCodes.QS40002,
                    "Failed to start workflow due to non-existent workflow or invalid version"
            );
        }
    }

    @Override
    public Object executeSync(String workflowName, TriggerWorkflowRequest request) {
        try {
            int version = Objects.isNull(request.getVersion()) ? -1 : request.getVersion();
            String resultKey = Objects.isNull(request.getResultKey()) ? "result" : request.getResultKey();
            String errorKey = Objects.isNull(request.getErrorKey()) ? "error" : request.getErrorKey();

            ProcessInstanceResult processInstance = camundaClient.newCreateInstanceCommand()
                    .bpmnProcessId(workflowName)
                    .version(version)
                    .variables(request.getVariables())
                    .withResult()
                    .send()
                    .join();

            log.info("Started workflow '{}' with definitionKey={} and instanceKey={}, returns resultKey={}",
                    workflowName,
                    processInstance.getProcessDefinitionKey(),
                    processInstance.getProcessInstanceKey(),
                    resultKey
            );

            Object data = processInstance.getVariable(resultKey);
            if (data instanceof Map<?, ?> rawMap) {
                Object errorObj = rawMap.get(errorKey);
                if (errorObj instanceof Map<?, ?> errorMap) {
                    String code = String.valueOf(
                            Objects.requireNonNullElse(errorMap.get("code"), ErrorCodes.QS00003)
                    );
                    String message = String.valueOf(
                            Objects.requireNonNullElse(errorMap.get("message"), "System busy")
                    );
                    log.error("Workflow error detected: code={}, message={}", code, message);
                    throw CommandExceptionBuilder.exception(code, message);
                }
            }

            return data;
        } catch (ClientException e) {
            log.error("Failed to start workflow '{}': {}", workflowName, e.getMessage(), e);
            throw CommandExceptionBuilder.exception(
                    ErrorCodes.QS40001,
                    e.getMessage()
            );
        }
    }
}
