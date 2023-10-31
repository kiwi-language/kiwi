package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ClassInstanceParam.class),
                @JsonSubTypes.Type(name = "2", value = ArrayParamDTO.class),
                @JsonSubTypes.Type(name = "3", value = PrimitiveParamDTO.class),
        }
)
public interface InstanceParamDTO {

    int getType();

}
