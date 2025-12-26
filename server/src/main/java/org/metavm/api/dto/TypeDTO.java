package org.metavm.api.dto;

import org.jsonk.Json;
import org.jsonk.SubType;

@Json(
        typeProperty = "kind",
        subTypes = {
                @SubType(value ="primitive", type = PrimitiveTypeDTO.class),
                @SubType(value ="class", type = ClassTypeDTO.class),
                @SubType(value ="array", type = ArrayTypeDTO.class),
                @SubType(value ="union", type = UnionTypeDTO.class),
        }
)
public interface TypeDTO {

    String getKind();

}
