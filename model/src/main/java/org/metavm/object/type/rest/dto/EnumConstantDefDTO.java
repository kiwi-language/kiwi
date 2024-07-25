package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.ValueDTO;

import javax.annotation.Nullable;
import java.util.List;

public record EnumConstantDefDTO(
        @Nullable String id,
        String name,
        int ordinal,
        List<ValueDTO> arguments
) {
}
