package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record EnumTypeParam(
        List<EnumConstantDTO> enumConstants
) {
}
