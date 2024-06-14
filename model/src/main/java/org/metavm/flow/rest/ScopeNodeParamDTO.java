package org.metavm.flow.rest;

public abstract class ScopeNodeParamDTO{
    private final ScopeDTO bodyScope;

    protected ScopeNodeParamDTO(ScopeDTO bodyScope) {
        this.bodyScope = bodyScope;
    }

    public ScopeDTO getBodyScope() {
        return bodyScope;
    }
}
