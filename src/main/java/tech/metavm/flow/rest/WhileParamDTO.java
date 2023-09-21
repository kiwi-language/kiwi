package tech.metavm.flow.rest;

import java.util.List;

public class WhileParamDTO extends LoopParamDTO {
    public WhileParamDTO(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        super(condition, loopScope, fields);
    }
}
