package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.common.enums.ExecutionMode;
import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;
import vn.com.vds.vdt.servicebuilder.service.core.WorkflowService;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("all")
@ResponseWrapper
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping("/{workflowName}")
    public Object execute(
            @PathVariable("workflowName") String workflowName,
            @RequestBody TriggerWorkflowRequest request,
            @RequestParam(value = "mode", defaultValue = "ASYNC") ExecutionMode mode
    ) {
        if (mode == ExecutionMode.SYNC) {
            log.info("Executing workflow '{}' in SYNC mode", workflowName);
            return workflowService.executeSync(workflowName, request);
        }
        workflowService.execute(workflowName, request);
        return null;
    }
}
