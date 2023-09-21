package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class SubFlowParam extends CallParamDTO {

    private final ValueDTO self;

    public SubFlowParam(@JsonProperty("self") ValueDTO self,
                        @JsonProperty("flowRef") RefDTO flowRef,
                        @JsonProperty("typeRef") @Nullable RefDTO typeRef,
                        @JsonProperty("fields") List<FieldParamDTO> fields) {
        super(flowRef, typeRef, fields);
        this.self = self;
    }

    public ValueDTO getSelf() {
        return self;
    }

}
