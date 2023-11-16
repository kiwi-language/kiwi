package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

public record ArrayTypeParam(
        RefDTO elementTypeRef,
        int kind
) implements TypeParam {
    @Override
    public int getType() {
        return 2;
    }

    @Override
    public TypeKey getTypeKey() {
        return new ArrayTypeKey(kind, elementTypeRef);
    }
}
