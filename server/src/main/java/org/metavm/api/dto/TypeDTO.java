package org.metavm.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = PrimitiveTypeDTO.class, name = "primitive"),
                @JsonSubTypes.Type(value = ClassTypeDTO.class, name = "class"),
                @JsonSubTypes.Type(value = ArrayTypeDTO.class, name = "array"),
                @JsonSubTypes.Type(value = UnionTypeDTO.class, name = "union"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface TypeDTO {

    String getKind();

}
