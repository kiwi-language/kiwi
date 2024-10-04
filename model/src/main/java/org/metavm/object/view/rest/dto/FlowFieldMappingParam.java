package org.metavm.object.view.rest.dto;

import org.metavm.flow.rest.MethodRefDTO;

import javax.annotation.Nullable;

public record FlowFieldMappingParam(
        MethodRefDTO getterRef,
        @Nullable MethodRefDTO setterRef
) implements FieldMappingParam{

    @Override
    public int getKind() {
        return 2;
    }
}
