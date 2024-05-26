package tech.metavm.flow.rest;

public record LambdaNodeRefDTO(
        String lambdaNodeId
) implements CallableRefDTO {
    @Override
    public int getKind() {
        return 3;
    }
}
