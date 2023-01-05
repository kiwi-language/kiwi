package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

public record UniqueConstraintItemDTO(
        Long id,
        String name,
        ValueDTO value
) {
}
