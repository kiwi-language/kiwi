package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.rest.dto.TypeDTO;

public class PrimitiveTypeFactory {

    public static PrimitiveType create(long id, String name, SQLColumnType columnType) {
        return new PrimitiveType(
                    new TypeDTO(
                        id,
                        name,
                        TypeCategory.PRIMITIVE.code(),
                        true,
                        false,
                        null,
                        null,
                        "基础类型",
                        null,
                        null
                ),
                columnType
        );
    }

}
