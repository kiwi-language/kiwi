package org.metavm.flow.rest;

import java.util.List;

public final class ForeachNodeParam extends LoopParamDTO {
    private final ValueDTO array;

    public ForeachNodeParam(
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
