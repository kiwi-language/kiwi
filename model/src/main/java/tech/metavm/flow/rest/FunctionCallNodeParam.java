package tech.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam {
    public FunctionCallNodeParam(@Nullable String flowId,
                                 @Nullable String typeId,
                                 List<ArgumentDTO> arguments,
                                 List<String> capturedExpressionTypeIds,
                                 List<String> capturedExpressions) {
        super(flowId, null, typeId, arguments, null, capturedExpressionTypeIds, capturedExpressions);
    }

    @Override
    public int getCallKind() {
        return 1;
    }
}
