package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = MethodRefDTO.class, name = "1"),
                @JsonSubTypes.Type(value = FunctionRefDTO.class, name = "2"),
                @JsonSubTypes.Type(value = LambdaNodeRefDTO.class, name = "3")
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface CallableRefDTO {

    int kind();

}
