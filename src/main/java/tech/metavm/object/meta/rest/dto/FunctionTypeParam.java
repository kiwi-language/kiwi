package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record FunctionTypeParam(
        List<RefDTO> parameterTypeRefs,
        RefDTO returnTypeRef
) implements TypeParam {
    @Override
    public int getType() {
        return 6;
    }
}
