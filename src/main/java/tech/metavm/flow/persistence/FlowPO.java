package tech.metavm.flow.persistence;

public class FlowPO {
    private Long id;

    private Long tenantId;

    private String name;

    private Long typeId;

    private Long rootScopeId;


    public FlowPO() {
    }

    public FlowPO(Long id, Long tenantId, String name, Long typeId, Long rootScopeId) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.typeId = typeId;
        this.rootScopeId = rootScopeId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getRootScopeId() {
        return rootScopeId;
    }

    public void setRootScopeId(Long rootScopeId) {
        this.rootScopeId = rootScopeId;
    }
}