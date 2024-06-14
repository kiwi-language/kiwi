package org.metavm.object.type.rest.dto;

import java.util.List;

public record IntersectionTypeParam(
        List<String> typeIds
) implements TypeParam{

    @Override
    public int getType() {
        return 8;
    }

}
