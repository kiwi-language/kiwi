package org.metavm.flow.rest;

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
    private final List<String> capturedVariableTypes;
    private final List<Long> capturedVariableIndexes;

    public CallNodeParam(@Nullable FlowRefDTO flowRef,
                         @Nullable String flowCode,
                         @Nullable List<String> typeArguments,
                         @Nullable String type,
                         List<String> capturedVariableTypes,
                         List<Long> capturedVariableIndexes) {
        this.flowRef = flowRef;
        this.flowCode = flowCode;
        this.typeArguments = typeArguments;
        this.type = type;
       this.capturedVariableTypes = capturedVariableTypes;
        this.capturedVariableIndexes = capturedVariableIndexes;
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

    public abstract int getCallKind();

    public List<String> getCapturedVariableTypes() {
        return capturedVariableTypes;
    }

    public List<Long> getCapturedVariableIndexes() {
        return capturedVariableIndexes;
    }

}
