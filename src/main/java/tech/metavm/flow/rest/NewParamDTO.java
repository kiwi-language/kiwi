package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class NewParamDTO extends CallParamDTO{

    public NewParamDTO(@JsonProperty("flowRef") RefDTO flowRef,
                       @JsonProperty("typeRef") @Nullable RefDTO typeRef,
                       @JsonProperty("arguments") List<ArgumentDTO> arguments) {
        super(flowRef, typeRef, arguments);
    }
}
