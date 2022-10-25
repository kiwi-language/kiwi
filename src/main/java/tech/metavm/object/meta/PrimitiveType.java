package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.rest.dto.TypeDTO;

public record PrimitiveType(
    TypeDTO typeDTO,
    SQLColumnType columnType
) {

    public long id() {
        return typeDTO.id();
    }

}
