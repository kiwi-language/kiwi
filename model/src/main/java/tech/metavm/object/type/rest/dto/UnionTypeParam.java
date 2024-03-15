package tech.metavm.object.type.rest.dto;

import java.util.HashSet;
import java.util.List;

public record UnionTypeParam(
        List<String> memberIds
) implements TypeParam {
    @Override
    public int getType() {
        return 4;
    }

    @Override
    public TypeKey getTypeKey() {
        return new UnionTypeKey(new HashSet<>(memberIds));
    }
}
