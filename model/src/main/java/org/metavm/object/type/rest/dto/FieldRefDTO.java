package org.metavm.object.type.rest.dto;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;

public record FieldRefDTO(
        String declaringType,
        String rawFieldId
) implements Copyable<FieldRefDTO> {
    @Override
    public FieldRefDTO copy(CopyContext context) {
        return new FieldRefDTO(declaringType, context.mapId(rawFieldId));
    }
}
