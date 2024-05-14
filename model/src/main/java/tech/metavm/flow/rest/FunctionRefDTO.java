package tech.metavm.flow.rest;

import java.util.List;

public record FunctionRefDTO(
    String rawFlowId,
    List<String> typeArguments
) implements FlowRefDTO {
    @Override
    public int kind() {
        return 2;
    }
}
