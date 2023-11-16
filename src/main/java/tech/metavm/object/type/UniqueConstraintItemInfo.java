package tech.metavm.object.type;

import tech.metavm.flow.rest.ValueDTO;

public record UniqueConstraintItemInfo(
        String name,
        ValueDTO value
) {

    public IndexFieldDTO toDTO() {
        return new IndexFieldDTO(null, name, null, value);
    }

}
