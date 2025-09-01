package vn.com.vds.vdt.servicebuilder.service.core;

import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;

import java.util.Map;

public interface WorkflowService {
    void execute(String workflowName, TriggerWorkflowRequest request);

    Map<String, Object> executeSync(String workflowName, TriggerWorkflowRequest request);
}
