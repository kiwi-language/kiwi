package org.metavm.flow.rest;

import java.util.List;

public abstract class LoopNodeParam extends ScopeNodeParam {
    private final ValueDTO condition;
    private final List<LoopFieldDTO> fields;

    protected LoopNodeParam(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        super(loopScope);
        this.condition = condition;
        this.fields = fields;
    }

    public ValueDTO getCondition() {
        return condition;
    }

    public List<LoopFieldDTO> getFields() {
        return fields;
    }
}
