package tech.metavm.flow.persistence;

import tech.metavm.entity.EntityPO;
import tech.metavm.entity.IndexDef;
import tech.metavm.flow.ScopeRT;

public class ScopePO extends EntityPO {

    public static final IndexDef<ScopeRT> INDEX_FLOW_ID = new IndexDef<>(
            ScopeRT.class, "flowId"
    );


    private Long id;
    private Long flowId;
    private Long deletedAt;

    public ScopePO(Long id, Long flowId) {
        this.id = id;
        this.flowId = flowId;
    }

    public ScopePO() {
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }
}