package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = MethodRefDTO.class, name = "1"),
                @JsonSubTypes.Type(value = FunctionRefDTO.class, name = "2"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface FlowRefDTO extends CallableRefDTO {

    String rawFlowId();

    List<String> typeArguments();

    int getKind();

}
