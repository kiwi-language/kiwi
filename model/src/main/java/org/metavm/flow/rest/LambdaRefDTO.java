package org.metavm.flow.rest;

public record LambdaRefDTO(
        String lambdaNodeId
) implements CallableRefDTO {
    @Override
    public int getKind() {
        return 3;
    }
}
