package tech.metavm.object.meta.rest.dto;

import org.jetbrains.annotations.Nullable;
import tech.metavm.dto.RefDTO;

import java.util.HashSet;
import java.util.List;

public record IntersectionTypeParam(
        List<RefDTO> typeRefs
) implements TypeParam{

    @Override
    public int getType() {
        return 8;
    }

    @Override
    public TypeKey getTypeKey() {
        return new IntersectionTypeKey(new HashSet<>(typeRefs));
    }
}
