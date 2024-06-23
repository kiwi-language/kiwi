package org.metavm.object.type.rest.dto;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.util.NncUtils;

import java.util.List;

public record BatchSaveRequest(
        List<? extends TypeDefDTO> typeDefs,
        List<FlowDTO> functions,
        boolean skipFlowPreprocess
) implements Copyable<BatchSaveRequest> {
    @Override
    public BatchSaveRequest copy(CopyContext context) {
        return new BatchSaveRequest(
                NncUtils.map(typeDefs, context::copy),
                NncUtils.map(functions, context::copy),
                skipFlowPreprocess
        );
    }
}
