package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;

public class TryNodeParam extends ScopeNodeParam implements Copyable<TryNodeParam> {

    public TryNodeParam(@JsonProperty("bodyScope") ScopeDTO bodyScope) {
        super(bodyScope);
    }

    @Override
    public TryNodeParam copy(CopyContext context) {
        return new TryNodeParam(context.copy(getBodyScope()));
    }
}
