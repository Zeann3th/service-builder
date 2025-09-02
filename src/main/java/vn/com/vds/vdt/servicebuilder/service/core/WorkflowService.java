package vn.com.vds.vdt.servicebuilder.service.core;

import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;

public interface WorkflowService {
    void execute(String workflowName, TriggerWorkflowRequest request);

    Object executeSync(String workflowName, TriggerWorkflowRequest request);
}
