package org.metavm.expression.dto;

public class FlowParsingContextDTO extends ParsingContextDTO {

    private String prevNodeId;
    private String scopeId;

    public FlowParsingContextDTO() {
        super(ContextTypes.CONTEXT_TYPE_FLOW);
    }

    public String getPrevNodeId() {
        return prevNodeId;
    }

    public void setPrevNodeId(String prevNodeId) {
        this.prevNodeId = prevNodeId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }
}
