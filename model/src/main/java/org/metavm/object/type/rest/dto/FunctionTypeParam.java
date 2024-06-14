package org.metavm.object.type.rest.dto;

import java.util.List;

public record FunctionTypeParam(
        List<String> parameterTypeIds,
        String returnTypeId
) implements TypeParam {
    @Override
    public int getType() {
        return 6;
    }

}
