package tech.metavm.expression.dto;

public class FlowParsingContextDTO extends ParsingContextDTO {

    private Long prevNodeId;
    private long scopeId;

    public FlowParsingContextDTO() {
        super(ContextTypes.CONTEXT_TYPE_FLOW);
    }

    public Long getPrevNodeId() {
        return prevNodeId;
    }

    public void setPrevNodeId(Long prevNodeId) {
        this.prevNodeId = prevNodeId;
    }

    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }
}
