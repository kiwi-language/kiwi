package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;

import java.util.List;

public record MethodRefDTO(
        String declaringType,
        String rawFlowId,
        List<String> typeArguments
) implements FlowRefDTO, Copyable<MethodRefDTO> {
    @Override
    public int getKind() {
        return 1;
    }

    @Override
    public MethodRefDTO copy(CopyContext context) {
        return new MethodRefDTO(declaringType, context.mapId(rawFlowId), typeArguments);
    }
}
