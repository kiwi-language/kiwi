package tech.metavm.object.type;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.type.rest.dto.IndexFieldDTO;

public record UniqueConstraintItemInfo(
        String name,
        ValueDTO value
) {

    public IndexFieldDTO toDTO() {
        return new IndexFieldDTO(null, name, null, value);
    }

}
