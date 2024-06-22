package org.metavm.flow.rest;

import java.util.List;

public class WhileNodeNodeParam extends LoopNodeParam {
    public WhileNodeNodeParam(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        super(condition, loopScope, fields);
    }
}
