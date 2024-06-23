package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import java.util.List;

public class WhileNodeNodeParam extends LoopNodeParam implements Copyable<WhileNodeNodeParam> {
    public WhileNodeNodeParam(ValueDTO condition, ScopeDTO loopScope, List<LoopFieldDTO> fields) {
        super(condition, loopScope, fields);
    }

    @Override
    public WhileNodeNodeParam copy(CopyContext context) {
        return new WhileNodeNodeParam(context.copy(getCondition()), context.copy(getBodyScope()), NncUtils.map(getFields(), context::copy));
    }
}
