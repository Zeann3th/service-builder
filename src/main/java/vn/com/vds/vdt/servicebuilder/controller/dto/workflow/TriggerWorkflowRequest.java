package vn.com.vds.vdt.servicebuilder.controller.dto.workflow;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerWorkflowRequest {
    private Integer version;
    private String resultKey;
    private Map<String, Object> variables;
}
