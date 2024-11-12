package org.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public class NewObjectNodeParam extends CallNodeParam {

    private final boolean ephemeral;

    private final boolean unbound;

    public NewObjectNodeParam(@Nullable MethodRefDTO flowRef,
                              @Nullable String flowCode,
                              @Nullable List<String> typeArgumentIds,
                              @Nullable String type,
                              boolean ephemeral,
                              boolean unbound,
                              List<String> capturedExpressionTypes,
                              List<Long> capturedVariables
    ) {
        super(flowRef, flowCode, typeArgumentIds, type, capturedExpressionTypes, capturedVariables);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isUnbound() {
        return unbound;
    }

    @Nullable
    @Override
    public MethodRefDTO getFlowRef() {
        return (MethodRefDTO) super.getFlowRef();
    }

    @Override
    public int getCallKind() {
        return 3;
    }

}
