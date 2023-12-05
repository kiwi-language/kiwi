package tech.metavm.expression.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "contextType", visible = true)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = TypeParsingContextDTO.class),
                @JsonSubTypes.Type(name = "2", value = FlowParsingContextDTO.class),
        }
)
public abstract class ParsingContextDTO {

    private final int contextType;

    public ParsingContextDTO(int contextType) {
        this.contextType = contextType;
    }

    public int getContextType() {
        return contextType;
    }

}
