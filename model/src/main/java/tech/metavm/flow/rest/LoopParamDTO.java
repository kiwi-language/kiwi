package tech.metavm.flow.rest;

import java.util.List;

public abstract class LoopParamDTO extends ScopeNodeParamDTO {
    private final ValueDTO condition;
    private final List<LoopFieldDTO> fields;

    protected LoopParamDTO(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
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
