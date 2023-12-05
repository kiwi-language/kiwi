package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public record FunctionTypeParam(
        List<RefDTO> parameterTypeRefs,
        RefDTO returnTypeRef
) implements TypeParam {
    @Override
    public int getType() {
        return 6;
    }

    @Override
    public TypeKey getTypeKey() {
        return new FunctionTypeKey(parameterTypeRefs, returnTypeRef);
    }
}
