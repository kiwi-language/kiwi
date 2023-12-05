package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public final class AddObjectParam extends ScopeNodeParamDTO implements NewParam<AddObjectParam> {
    private final RefDTO typeRef;
    private final Boolean initializeArrayChildren;
    private final List<FieldParamDTO> fieldParams;
    @Nullable
    private final ParentRefDTO parentRef;

    public AddObjectParam(
            RefDTO typeRef,
            Boolean initializeArrayChildren,
            List<FieldParamDTO> fieldParams,
            @Nullable ParentRefDTO parentRef,
            ScopeDTO bodyScope
    ) {
        super(bodyScope);
        this.typeRef = typeRef;
        this.initializeArrayChildren = initializeArrayChildren;
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

    @Override
    public AddObjectParam copyWithParentRef(ParentRefDTO parentRef) {
        return new AddObjectParam(typeRef, initializeArrayChildren, fieldParams, parentRef, getBodyScope());
    }
}
