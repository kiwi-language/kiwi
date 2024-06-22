package org.metavm.flow.rest;

import java.util.List;

public final class ForeachNodeNodeParam extends LoopNodeParam {
    private final ValueDTO array;

    public ForeachNodeNodeParam(
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
