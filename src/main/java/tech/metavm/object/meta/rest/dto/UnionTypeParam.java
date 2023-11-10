package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.HashSet;
import java.util.List;

public record UnionTypeParam(
        List<RefDTO> memberRefs
) implements TypeParam {
    @Override
    public int getType() {
        return 4;
    }

    @Override
    public TypeKey getTypeKey() {
        return new UnionTypeKey(new HashSet<>(memberRefs));
    }
}
