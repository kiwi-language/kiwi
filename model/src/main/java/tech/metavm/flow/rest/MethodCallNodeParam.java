package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;

public class MethodCallNodeParam extends CallNodeParam {

    private final ValueDTO self;

    public MethodCallNodeParam(@JsonProperty("self") ValueDTO self,
                               @JsonProperty("flowId") String flowId,
                               @JsonProperty("typeId") @Nullable String typeId,
                               @JsonProperty("arguments") List<ArgumentDTO> arguments,
                               @JsonProperty("capturedExpressionTypeIds") List<String> capturedExpressionTypeIds,
                               @JsonProperty("capturedExpressions") List<String> capturedExpressions
    ) {
        super(flowId, typeId, arguments, capturedExpressionTypeIds, capturedExpressions);
        this.self = self;
    }

    public ValueDTO getSelf() {
        return self;
    }

    @Override
    public int getCallKind() {
        return 2;
    }
}
