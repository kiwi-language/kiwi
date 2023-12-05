package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TryNodeParamDTO extends ScopeNodeParamDTO {

    public TryNodeParamDTO(@JsonProperty("bodyScope") ScopeDTO bodyScope) {
        super(bodyScope);
    }

}
