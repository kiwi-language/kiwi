package org.metavm.object.view.rest.dto;

import java.util.List;

public record FieldsObjectMappingParam(
        List<FieldMappingDTO> fieldMappings
) implements ObjectMappingParam {

    @Override
    public int getKind() {
        return 1;
    }
}
