package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public final class AddObjectNodeParam extends ScopeNodeParamDTO implements NewParam<AddObjectNodeParam> {
    private final RefDTO typeRef;
    private final Boolean initializeArrayChildren;
    private final List<FieldParamDTO> fieldParams;
    private final boolean ephemeral;
    @Nullable
    private final ParentRefDTO parentRef;

    public AddObjectNodeParam(
            RefDTO typeRef,
            Boolean initializeArrayChildren,
            boolean ephemeral,
            List<FieldParamDTO> fieldParams,
            @Nullable ParentRefDTO parentRef,
            ScopeDTO bodyScope
    ) {
        super(bodyScope);
        this.typeRef = typeRef;
        this.initializeArrayChildren = initializeArrayChildren;
        this.ephemeral = ephemeral;
        this.fieldParams = fieldParams;
        this.parentRef = parentRef;
    }

    public RefDTO getTypeRef() {
        return typeRef;
    }

    public List<FieldParamDTO> getFieldParams() {
        return fieldParams;
    }

    @Nullable
    public ParentRefDTO getParentRef() {
        return parentRef;
    }

    public Boolean isInitializeArrayChildren() {
        return initializeArrayChildren;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    @Override
    public AddObjectNodeParam copyWithParentRef(ParentRefDTO parentRef) {
        return new AddObjectNodeParam(typeRef, initializeArrayChildren, ephemeral, fieldParams, parentRef, getBodyScope());
    }
}
