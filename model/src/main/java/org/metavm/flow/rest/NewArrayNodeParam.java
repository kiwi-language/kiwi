package org.metavm.flow.rest;

public record NewArrayNodeParam(
        ValueDTO value,
        ParentRefDTO parentRef
) implements NewParam<NewArrayNodeParam> {
    @Override
    public NewArrayNodeParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewArrayNodeParam(value, parentRef);
    }
}
