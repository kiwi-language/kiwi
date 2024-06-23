package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.object.type.rest.dto.FieldRefDTO;

public record FieldParamDTO (
        String id,
        FieldRefDTO fieldRef,
        ValueDTO value
) implements BaseDTO, Copyable<FieldParamDTO> {

    public static FieldParamDTO create(FieldRefDTO fieldRef, ValueDTO value) {
        return new FieldParamDTO(null, fieldRef, value);
    }

    @Override
    public FieldParamDTO copy(CopyContext context) {
        return new FieldParamDTO(context.mapId(id), context.copy(fieldRef), context.copy(value));
    }
}
