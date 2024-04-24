package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public final class AddObjectNodeParam extends ScopeNodeParamDTO implements NewParam<AddObjectNodeParam> {
    private final String type;
    private final Boolean initializeArrayChildren;
    private final List<FieldParamDTO> fieldParams;
    private final boolean ephemeral;
    @Nullable
    private final ParentRefDTO parentRef;

    public AddObjectNodeParam(
            String typeId,
            Boolean initializeArrayChildren,
            boolean ephemeral,
            List<FieldParamDTO> fieldParams,
            @Nullable ParentRefDTO parentRef,
            ScopeDTO bodyScope
    ) {
        super(bodyScope);
        this.type = typeId;
        this.initializeArrayChildren = initializeArrayChildren;
        this.ephemeral = ephemeral;
        this.fieldParams = fieldParams;
        this.parentRef = parentRef;
    }

    public String getType() {
        return type;
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
        return new AddObjectNodeParam(type, initializeArrayChildren, ephemeral, fieldParams, parentRef, getBodyScope());
    }
}
