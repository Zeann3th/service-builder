package vn.com.vds.vdt.servicebuilder.common.enums;

import lombok.Getter;

@Getter
public enum ExecutionMode {
    SYNC("SYNC"),
    ASYNC("ASYNC");

    private final String mode;

    ExecutionMode(String mode) {
        this.mode = mode;
    }
}
