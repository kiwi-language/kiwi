package org.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;
import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam implements Copyable<FunctionCallNodeParam> {
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

    @Override
    public FunctionCallNodeParam copy(CopyContext context) {
        return new FunctionCallNodeParam(
                context.copy(getFlowRef()),
                getType(),
                NncUtils.map(getArguments(), context::copy),
                getCapturedExpressionTypes(),
                getCapturedExpressions()
        );
    }
}
