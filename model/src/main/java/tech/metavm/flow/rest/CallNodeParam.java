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
    private final FlowRefDTO flowRef;
    private final @Nullable String flowCode;
    private final @Nullable List<String> typeArguments;
    private final @Nullable String type;
    private final List<ArgumentDTO> arguments;
    private final List<ValueDTO> argumentValues;
    private final List<String> capturedExpressionTypes;
    private final List<String> capturedExpressions;

    public CallNodeParam(@Nullable FlowRefDTO flowRef,
                         @Nullable String flowCode,
                         @Nullable List<String> typeArguments,
                         @Nullable String type,
                         List<ArgumentDTO> arguments,
                         List<ValueDTO> argumentValues,
                         List<String> capturedExpressionTypes,
                         List<String> capturedExpressions) {
        this.flowRef = flowRef;
        this.flowCode = flowCode;
        this.typeArguments = typeArguments;
        this.type = type;
        this.arguments = arguments;
        this.argumentValues = argumentValues;
        this.capturedExpressionTypes = capturedExpressionTypes;
        this.capturedExpressions = capturedExpressions;
    }

    public boolean isResolved() {
        return flowRef != null;
    }

    public @Nullable FlowRefDTO getFlowRef() {
        return flowRef;
    }

    @Nullable
    public String getFlowCode() {
        return flowCode;
    }

    @Nullable
    public List<String> getTypeArguments() {
        return typeArguments;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public List<ArgumentDTO> getArguments() {
        return arguments;
    }

    public List<ValueDTO> getArgumentValues() {
        return argumentValues;
    }

    public abstract int getCallKind();

    public List<String> getCapturedExpressionTypes() {
        return capturedExpressionTypes;
    }

    public List<String> getCapturedExpressions() {
        return capturedExpressions;
    }

}
