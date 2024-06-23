package org.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NewObjectNodeParam extends CallNodeParam implements NewParam<NewObjectNodeParam> {

    @Nullable
    private final ParentRefDTO parent;

    private final boolean ephemeral;

    private final boolean unbound;

    public NewObjectNodeParam(@Nullable MethodRefDTO flowRef,
                              @Nullable String flowCode,
                              @Nullable List<String> typeArgumentIds,
                              @Nullable String type,
                              List<ArgumentDTO> arguments,
                              List<ValueDTO> argumentValues,
                              @Nullable ParentRefDTO parent,
                              boolean ephemeral,
                              boolean unbound,
                              List<String> capturedExpressionTypes,
                              List<String> capturedExpressions
    ) {
        super(flowRef, flowCode, typeArgumentIds, type, arguments, argumentValues, capturedExpressionTypes, capturedExpressions);
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

    @Nullable
    @Override
    public MethodRefDTO getFlowRef() {
        return (MethodRefDTO) super.getFlowRef();
    }

    @Override
    public NewObjectNodeParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewObjectNodeParam(
                getFlowRef(), getFlowCode(), getTypeArguments(), getType(), getArguments(), getArgumentValues(), parentRef, ephemeral, unbound,
                new ArrayList<>(getCapturedExpressionTypes()), new ArrayList<>(getCapturedExpressions())
        );
    }

    @Override
    public int getCallKind() {
        return 3;
    }

}
