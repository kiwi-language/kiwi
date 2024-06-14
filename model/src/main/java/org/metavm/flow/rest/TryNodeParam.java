package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TryNodeParam extends ScopeNodeParamDTO {

    public TryNodeParam(@JsonProperty("bodyScope") ScopeDTO bodyScope) {
        super(bodyScope);
    }

}
