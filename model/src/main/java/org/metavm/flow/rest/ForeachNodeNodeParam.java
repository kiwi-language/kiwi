package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import java.util.List;

public final class ForeachNodeNodeParam extends LoopNodeParam implements Copyable<ForeachNodeNodeParam> {
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


    @Override
    public ForeachNodeNodeParam copy(CopyContext context) {
        return new ForeachNodeNodeParam(
                context.copy(array),
                context.copy(getCondition()),
                NncUtils.map(getFields(), context::copy),
                context.copy(getBodyScope())
        );
    }
}
