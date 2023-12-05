package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ClassInstanceParam.class),
                @JsonSubTypes.Type(name = "2", value = ArrayInstanceParam.class),
                @JsonSubTypes.Type(name = "3", value = PrimitiveInstanceParam.class),
        }
)
public interface InstanceParam {

    int getType();

}
