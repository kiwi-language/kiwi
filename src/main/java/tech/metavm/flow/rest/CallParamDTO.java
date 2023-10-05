package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CallParamDTO{
    private final RefDTO flowRef;
    @Nullable
    private final RefDTO typeRef;
    private final List<ArgumentDTO> arguments;

    public CallParamDTO(RefDTO flowRef, @Nullable RefDTO typeRef, List<ArgumentDTO> arguments) {
        this.flowRef = flowRef;
        this.typeRef = typeRef;
        this.arguments = arguments;
    }


    public RefDTO getFlowRef() {
        return flowRef;
    }

    @Nullable
    public RefDTO getTypeRef() {
        return typeRef;
    }

    public List<ArgumentDTO> getArguments() {
        return arguments;
    }
}
