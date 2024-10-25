package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TryEnterNodeParam extends ScopeNodeParam {

    public TryEnterNodeParam(@JsonProperty("bodyScope") ScopeDTO bodyScope) {
        super(bodyScope);
    }

}
