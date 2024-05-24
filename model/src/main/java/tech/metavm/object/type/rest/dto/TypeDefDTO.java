package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.metavm.common.BaseDTO;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = TypeDTO.class, name = "1"),
                @JsonSubTypes.Type(value = TypeVariableDTO.class, name = "2"),
                @JsonSubTypes.Type(value = CapturedTypeVariableDTO.class, name = "3"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "defKind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)

public interface TypeDefDTO extends BaseDTO  {

    int getDefKind();

}
