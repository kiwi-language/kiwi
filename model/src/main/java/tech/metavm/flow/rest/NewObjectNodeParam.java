package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class NewObjectNodeParam extends CallNodeParam implements NewParam<NewObjectNodeParam> {

    @Nullable
    private final ParentRefDTO parent;

    private final boolean ephemeral;

    private final boolean unbound;

    public NewObjectNodeParam(RefDTO flowRef,
                              @Nullable RefDTO typeRef,
                              List<ArgumentDTO> arguments,
                              @Nullable ParentRefDTO parent,
                              boolean ephemeral,
                              boolean unbound
    ) {
        super(flowRef, typeRef, arguments);
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
                getFlowRef(), getTypeRef(), getArguments(), parentRef, ephemeral, unbound
        );
    }

    @Override
    public int getCallKind() {
        return 3;
    }
}
