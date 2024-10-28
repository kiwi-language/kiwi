package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.flow.rest.MethodRefDTO;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = MethodRefDTO.class, name = "1"),
                @JsonSubTypes.Type(value = FieldRefDTO.class, name = "2"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface PropertyRefDTO {

    int getKind();

}
