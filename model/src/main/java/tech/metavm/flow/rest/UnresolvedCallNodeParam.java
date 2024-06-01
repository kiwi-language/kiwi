package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public abstract class UnresolvedCallNodeParam {
    private final String flowName;
    private final @Nullable String typeId;
    private final List<ValueDTO> arguments;

    public UnresolvedCallNodeParam(String flowName,
                                   @Nullable String typeId,
                                   List<ValueDTO> arguments) {
        this.flowName = flowName;
        this.typeId = typeId;
        this.arguments = arguments;
    }

    public String getFlowName() {
        return flowName;
    }

    @Nullable
    public String getTypeId() {
        return typeId;
    }

    public List<ValueDTO> getArguments() {
        return arguments;
    }

    public abstract int getCallKind();

}
