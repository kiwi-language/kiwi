package tech.metavm.object.meta;

import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;

public record UniqueConstraintItemInfo(
        String name,
        ValueDTO value
) {

    public UniqueConstraintItemDTO toDTO() {
        return new UniqueConstraintItemDTO(null, name, value);
    }

}
