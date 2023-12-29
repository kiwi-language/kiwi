package tech.metavm.flow.rest;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;

import java.util.List;

public class FunctionCallNodeParam extends CallNodeParam {
    public FunctionCallNodeParam(RefDTO flowRef,
                                 @Nullable RefDTO typeRef,
                                 List<ArgumentDTO> arguments) {
        super(flowRef, typeRef, arguments);
    }

    @Override
    public int getCallKind() {
        return 1;
    }
}
