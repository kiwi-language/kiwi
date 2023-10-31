package tech.metavm.flow.persistence;

import tech.metavm.entity.EntityPO;
import tech.metavm.entity.Identifiable;
import tech.metavm.entity.IndexDef;
import tech.metavm.flow.NodeRT;
import tech.metavm.util.TypeReference;

public class NodePO extends EntityPO {

    public static final IndexDef<NodeRT<?>> INDEX_FLOW_ID
            = new IndexDef<>(new TypeReference<>() {}, false,"flowId");


    private Long id;

    private Long tenantId;

    private String name;

    private Long flowId;

    private Integer type;

    private Long prevId;

    private Long outputTypeId;

    private String param;

    private Long deletedAt;

    private Long scopeId;

    public NodePO(Long id,
                  Long tenantId,
                  String name,
                  Long flowId,
                  Integer type,
                  Long prevId,
                  Long outputTypeId,
                  Long scopeId,
                  String param,
                  Long deletedAt) {
        super(id, tenantId);
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.flowId = flowId;
        this.type = type;
        this.prevId = prevId;
        this.outputTypeId = outputTypeId;
        this.scopeId = scopeId;
        this.param = param;
        this.deletedAt = deletedAt;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setPrevId(Long prevId) {
        this.prevId = prevId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getPrevId() {
        return prevId;
    }

    public Long getOutputTypeId() {
        return outputTypeId;
    }

    public void setOutputTypeId(Long outputTypeId) {
        this.outputTypeId = outputTypeId;
    }

    public void setScopeId(Long scopeId) {
        this.scopeId = scopeId;
    }

    public Long getScopeId() {
        return scopeId;
    }
}