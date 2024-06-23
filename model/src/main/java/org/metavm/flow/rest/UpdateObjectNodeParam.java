package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import java.util.List;

public record UpdateObjectNodeParam(
        ValueDTO objectId,
        List<UpdateFieldDTO> fields
) implements Copyable<UpdateObjectNodeParam> {
    @Override
    public UpdateObjectNodeParam copy(CopyContext context) {
        return new UpdateObjectNodeParam(context.copy(objectId), NncUtils.map(fields, context::copy));
    }
}
