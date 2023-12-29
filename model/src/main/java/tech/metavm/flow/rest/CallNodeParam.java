package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.metavm.common.RefDTO;

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
    private final RefDTO flowRef;
    @Nullable
    private final RefDTO typeRef;
    private final List<ArgumentDTO> arguments;

    public CallNodeParam(RefDTO flowRef, @Nullable RefDTO typeRef, List<ArgumentDTO> arguments) {
        this.flowRef = flowRef;
        this.typeRef = typeRef;
        this.arguments = arguments;
    }

    public RefDTO getFlowRef() {
        return flowRef;
    }

    @Nullable
    public RefDTO getTypeRef() {
        return typeRef;
    }

    public List<ArgumentDTO> getArguments() {
        return arguments;
    }

    public abstract int getCallKind();

}
