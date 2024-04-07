package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NewObjectNodeParam extends CallNodeParam implements NewParam<NewObjectNodeParam> {

    @Nullable
    private final ParentRefDTO parent;

    private final boolean ephemeral;

    private final boolean unbound;

    public NewObjectNodeParam(String flowId,
                              @Nullable String typeId,
                              List<ArgumentDTO> arguments,
                              @Nullable ParentRefDTO parent,
                              boolean ephemeral,
                              boolean unbound,
                              List<String> capturedExpressionTypeIds,
                              List<String> capturedExpressions
    ) {
        super(flowId, typeId, arguments, capturedExpressionTypeIds, capturedExpressions);
        this.parent = parent;
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
    public ParentRefDTO getParent() {
        return parent;
    }

    @Override
    public NewObjectNodeParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewObjectNodeParam(
                getFlowId(), getTypeId(), getArguments(), parentRef, ephemeral, unbound,
                new ArrayList<>(getCapturedExpressionTypeIds()), new ArrayList<>(getCapturedExpressions())
        );
    }

    @Override
    public int getCallKind() {
        return 3;
    }
}
