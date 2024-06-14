package org.metavm.object.view.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = DirectFieldMappingParam.class, name = "1"),
                @JsonSubTypes.Type(value = FlowFieldMappingParam.class, name = "2"),
                @JsonSubTypes.Type(value = ComputedFieldMappingParam.class, name = "3"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface FieldMappingParam {

    int getKind();

}
