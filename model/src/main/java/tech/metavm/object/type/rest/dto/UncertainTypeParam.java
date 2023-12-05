package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

public record UncertainTypeParam(
        RefDTO lowerBoundRef,
        RefDTO upperBoundRef
) implements TypeParam {

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public TypeKey getTypeKey() {
        return new UncertainTypeKey(lowerBoundRef, upperBoundRef);
    }
}
