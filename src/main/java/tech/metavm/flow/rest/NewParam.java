package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class NewParam extends CallParam {

    @Nullable
    private final MasterRefDTO master;

    public NewParam(@JsonProperty("flowRef") RefDTO flowRef,
                    @JsonProperty("typeRef") @Nullable RefDTO typeRef,
                    @JsonProperty("arguments") List<ArgumentDTO> arguments,
                    @Nullable @JsonProperty("master") MasterRefDTO master
    ) {
        super(flowRef, typeRef, arguments);
        this.master = master;
    }

    @Nullable
    public MasterRefDTO getMaster() {
        return master;
    }

}
