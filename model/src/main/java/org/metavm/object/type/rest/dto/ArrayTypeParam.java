package org.metavm.object.type.rest.dto;

public record ArrayTypeParam(
        String elementTypeId,
        int kind
) implements TypeParam {
    @Override
    public int getType() {
        return 2;
    }

}
