package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.common.rest.dto.BaseDTO;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = KlassDTO.class, name = "1"),
                @JsonSubTypes.Type(value = TypeVariableDTO.class, name = "2"),
                @JsonSubTypes.Type(value = CapturedTypeVariableDTO.class, name = "3"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "defKind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface TypeDefDTO extends BaseDTO  {

    int getDefKind();

}
