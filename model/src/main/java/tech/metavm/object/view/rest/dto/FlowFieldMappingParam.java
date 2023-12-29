package tech.metavm.object.view.rest.dto;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;

public record FlowFieldMappingParam(
        RefDTO getterRef,
        @Nullable RefDTO setterRef
) implements FieldMappingParam{

    @Override
    public int getKind() {
        return 2;
    }
}
