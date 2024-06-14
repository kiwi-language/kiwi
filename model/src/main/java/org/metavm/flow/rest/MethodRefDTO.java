package org.metavm.flow.rest;

import java.util.List;

public record MethodRefDTO(
        String declaringType,
        String rawFlowId,
        List<String> typeArguments
) implements FlowRefDTO {
    @Override
    public int getKind() {
        return 1;
    }
}
