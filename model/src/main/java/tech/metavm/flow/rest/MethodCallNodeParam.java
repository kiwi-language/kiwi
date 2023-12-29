package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class MethodCallNodeParam extends CallNodeParam {

    private final ValueDTO self;

    public MethodCallNodeParam(@JsonProperty("self") ValueDTO self,
                               @JsonProperty("flowRef") RefDTO flowRef,
                               @JsonProperty("typeRef") @Nullable RefDTO typeRef,
                               @JsonProperty("arguments") List<ArgumentDTO> arguments) {
        super(flowRef, typeRef, arguments);
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
