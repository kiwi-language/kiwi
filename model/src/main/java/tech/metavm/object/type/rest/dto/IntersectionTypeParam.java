package tech.metavm.object.type.rest.dto;

import java.util.HashSet;
import java.util.List;

public record IntersectionTypeParam(
        List<String> typeIds
) implements TypeParam{

    @Override
    public int getType() {
        return 8;
    }

    @Override
    public TypeKey getTypeKey() {
        return new IntersectionTypeKey(new HashSet<>(typeIds));
    }
}
