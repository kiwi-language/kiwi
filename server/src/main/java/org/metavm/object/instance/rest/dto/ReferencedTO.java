package org.metavm.object.instance.rest.dto;

import org.metavm.api.dto.ClassTypeDTO;

public record ReferencedTO(
        String id,
        ClassTypeDTO type,
        String summary
) implements ValueDTO {
    @Override
    public String getKind() {
        return "reference";
    }
}
