package org.metavm.object.type.rest.dto;

import java.util.List;

public record EnumTypeParam(
        List<EnumConstantDTO> enumConstants
) {
}
