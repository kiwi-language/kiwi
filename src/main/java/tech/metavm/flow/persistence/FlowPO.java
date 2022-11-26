package tech.metavm.flow.persistence;

import tech.metavm.entity.EntityPO;
import tech.metavm.entity.IndexDef;
import tech.metavm.flow.FlowRT;

public class FlowPO extends EntityPO {

    public static final IndexDef<FlowRT> INDEX_DECLARING_TYPE_ID = new IndexDef<>(
            FlowRT.class, "declaringTypeId"
    );

    private Long id;

    private Long tenantId;

    private String name;

    private Long declaringTypeId;

    private Long rootScopeId;

    private Long inputTypeId;

    private Long outputTypeId;

    public FlowPO() {
    }

    public FlowPO(Long id, Long tenantId, String name, Long declaringTypeId, Long rootScopeId, Long inputTypeId, Long outputTypeId) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.declaringTypeId = declaringTypeId;
        this.rootScopeId = rootScopeId;
        this.inputTypeId = inputTypeId;
        this.outputTypeId = outputTypeId;
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

    public Long getDeclaringTypeId() {
        return declaringTypeId;
    }

    public void setDeclaringTypeId(Long declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
    }

    public Long getRootScopeId() {
        return rootScopeId;
    }

    public void setRootScopeId(Long rootScopeId) {
        this.rootScopeId = rootScopeId;
    }

    public Long getOutputTypeId() {
        return outputTypeId;
    }

    public void setOutputTypeId(Long outputTypeId) {
        this.outputTypeId = outputTypeId;
    }

    public Long getInputTypeId() {
        return inputTypeId;
    }

    public void setInputTypeId(Long inputTypeId) {
        this.inputTypeId = inputTypeId;
    }
}