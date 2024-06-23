package org.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam {
    public FunctionCallNodeParam(@Nullable FunctionRefDTO flowRef,
                                 @Nullable String type,
                                 List<ArgumentDTO> arguments,
                                 List<String> capturedExpressionTypes,
                                 List<String> capturedExpressions) {
        super(flowRef, null, null, type, arguments, null, capturedExpressionTypes, capturedExpressions);
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
