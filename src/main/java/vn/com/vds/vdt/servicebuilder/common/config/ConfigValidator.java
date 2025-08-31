package vn.com.vds.vdt.servicebuilder.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("all")
public class ConfigValidator {
    @Value("${spring.application.name}")
    private String serviceName;

    @PostConstruct
    public void validate() {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalStateException("Service name must be configured and cannot be empty");
        }
    }
}
