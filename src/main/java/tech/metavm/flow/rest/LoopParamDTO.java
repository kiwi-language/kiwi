package tech.metavm.flow.rest;

import java.util.List;

public abstract class LoopParamDTO {
    private final ValueDTO condition;
    private final ScopeDTO loopScope;
    private final List<LoopFieldDTO> fields;

    protected LoopParamDTO(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        this.condition = condition;
        this.loopScope = loopScope;
        this.fields = fields;
    }

    public ValueDTO getCondition() {
        return condition;
    }

    public ScopeDTO getLoopScope() {
        return loopScope;
    }

    public List<LoopFieldDTO> getFields() {
        return fields;
    }
}
