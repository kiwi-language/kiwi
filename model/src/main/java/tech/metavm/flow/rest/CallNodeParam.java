package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.annotation.Nullable;
import java.util.List;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = FunctionCallNodeParam.class, name = "1"),
                @JsonSubTypes.Type(value = MethodCallNodeParam.class, name = "2"),
                @JsonSubTypes.Type(value = NewObjectNodeParam.class, name = "3")
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "callKind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public abstract class CallNodeParam {
    private final String flowId;
    @Nullable
    private final String typeId;
    private final List<ArgumentDTO> arguments;

    public CallNodeParam(String flowId, @Nullable String typeId, List<ArgumentDTO> arguments) {
        this.flowId = flowId;
        this.typeId = typeId;
        this.arguments = arguments;
    }

    public String getFlowId() {
        return flowId;
    }

    @Nullable
    public String getTypeId() {
        return typeId;
    }

    public List<ArgumentDTO> getArguments() {
        return arguments;
    }

    public abstract int getCallKind();

}
