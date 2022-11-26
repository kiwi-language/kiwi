package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

public record UniqueConstraintItemDTO(
        String name,
        ValueDTO value
) {
}
