package tech.metavm.object.meta.rest.dto;

import java.util.List;
import java.util.Set;

public record UnionTypeParamDTO(
        List<TypeDTO> typeMembers
) {
}
