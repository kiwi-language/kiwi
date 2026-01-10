package org.manul.entity;

public record BootstrapResult(
        int numInstancesWithNullIds,
        DefContext defContext
) {
}
