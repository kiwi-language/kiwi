package org.metavm.object.view.rest.dto;

import org.metavm.flow.rest.ValueDTO;

public record ComputedFieldMappingParam(
        ValueDTO value
) implements FieldMappingParam {
    @Override
    public int getKind() {
        return 3;
    }
}
