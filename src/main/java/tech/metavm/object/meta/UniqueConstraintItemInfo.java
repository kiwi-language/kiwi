package tech.metavm.object.meta;

import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;

public record UniqueConstraintItemInfo(
        String name,
        ValueDTO value
) {

    public static UniqueConstraintItemInfo createForField(String name, long fieldId) {
        return new UniqueConstraintItemInfo(
                name,
                new ValueDTO(ValueKind.REFERENCE.code(), "$" + fieldId, null)
        );
    }

    public UniqueConstraintItemDTO toDTO() {
        return new UniqueConstraintItemDTO(name, value);
    }

}
