package org.metavm.object.type.rest.dto;

import javax.annotation.Nullable;

public record EnumConstantDefDTO(
        @Nullable String id,
        String name,
        int ordinal,
        String initializerId
) {
}
