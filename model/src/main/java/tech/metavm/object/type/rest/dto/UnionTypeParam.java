package tech.metavm.object.type.rest.dto;

import java.util.List;

public record UnionTypeParam(
        List<String> memberIds
) implements TypeParam {
    @Override
    public int getType() {
        return 4;
    }

}
