package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record UnionTypeParam(
        List<RefDTO> memberRefs
) implements TypeParam {
    @Override
    public int getType() {
        return 4;
    }
}
