package tech.metavm.flow.rest;

import java.util.List;

public final class ForEachParamDTO extends LoopParamDTO {
    private final ValueDTO array;

    public ForEachParamDTO(
            ValueDTO array,
            ValueDTO condition,
            List<LoopFieldDTO> fields,
            ScopeDTO loopScope
    ) {
        super(condition, loopScope, fields);
        this.array = array;
    }

    public ValueDTO getArray() {
        return array;
    }


}
