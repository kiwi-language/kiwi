package org.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam {
    public FunctionCallNodeParam(@Nullable FunctionRefDTO flowRef,
                                 @Nullable String type,
                                 List<String> capturedVariableTypes,
                                 List<Long> capturedVariableIndexes) {
        super(flowRef, null, null, type, capturedVariableTypes, capturedVariableIndexes);
    }

    @javax.annotation.Nullable
    @Override
    public FunctionRefDTO getFlowRef() {
        return (FunctionRefDTO) super.getFlowRef();
    }

    @Override
    public int getCallKind() {
        return 1;
    }

}
