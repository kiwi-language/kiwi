package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

public record ArrayTypeParam(
        RefDTO elementTypeRef,
        int kind
) implements TypeParam {
    @Override
    public int getType() {
        return 2;
    }
}
