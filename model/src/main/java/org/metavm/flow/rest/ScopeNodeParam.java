package org.metavm.flow.rest;

public abstract class ScopeNodeParam {
    private final ScopeDTO bodyScope;

    protected ScopeNodeParam(ScopeDTO bodyScope) {
        this.bodyScope = bodyScope;
    }

    public ScopeDTO getBodyScope() {
        return bodyScope;
    }
}
