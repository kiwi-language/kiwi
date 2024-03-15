package tech.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam {
    public FunctionCallNodeParam(String flowId,
                                 @Nullable String typeId,
                                 List<ArgumentDTO> arguments) {
        super(flowId, typeId, arguments);
    }

    @Override
    public int getCallKind() {
        return 1;
    }
}
