package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ClassInstanceParam.class),
                @JsonSubTypes.Type(name = "2", value = ArrayInstanceParam.class),
                @JsonSubTypes.Type(name = "3", value = PrimitiveInstanceParam.class),
                @JsonSubTypes.Type(name = "4", value = ListInstanceParam.class),
        }
)
public interface InstanceParam {

    boolean valueEquals(InstanceParam param1, Set<String> newIds);

    int getType();

}
