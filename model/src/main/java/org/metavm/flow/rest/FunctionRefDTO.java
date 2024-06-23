package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;

import java.util.List;

public record FunctionRefDTO(
    String rawFlowId,
    List<String> typeArguments
) implements FlowRefDTO, Copyable<FunctionRefDTO> {
    @Override
    public int getKind() {
        return 2;
    }

    @Override
    public FunctionRefDTO copy(CopyContext context) {
        return new FunctionRefDTO(context.mapId(rawFlowId), typeArguments);
    }
}
