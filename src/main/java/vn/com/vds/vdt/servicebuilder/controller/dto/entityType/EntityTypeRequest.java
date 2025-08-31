package vn.com.vds.vdt.servicebuilder.controller.dto.entityType;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EntityTypeRequest {
    private String name;
    private String displayName;
    private Boolean isActive;
    private List<AttributeCreate> attributes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class AttributeCreate {
        private String name;
        private String displayName;
        private String dataType; // matches DataType enum values
        private Boolean isRequired;
        private String defaultValue;
        private Map<String, String> validationRules;
    }
}
