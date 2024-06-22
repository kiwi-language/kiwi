package org.metavm.object.view.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.common.rest.dto.BaseDTO;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ObjectMappingDTO.class)
        }
)
public interface MappingDTO extends BaseDTO {

    int getKind();

}
