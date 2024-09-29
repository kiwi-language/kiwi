package org.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public record NewArrayNodeParam(
        ValueDTO value,
        @Nullable List<ValueDTO> dimensions,
        ParentRefDTO parentRef
) implements NewParam<NewArrayNodeParam> {
    @Override
    public NewArrayNodeParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewArrayNodeParam(value, dimensions, parentRef);
    }
}
