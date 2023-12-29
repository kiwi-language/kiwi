package tech.metavm.flow.rest;

import java.util.List;

public class WhileNodeParam extends LoopParamDTO {
    public WhileNodeParam(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        super(condition, loopScope, fields);
    }
}
