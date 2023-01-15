package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TargetPO {

    private Long tenantId;
    private Long targetId;
    private Integer kind;
    private Long fieldId;

    public TargetPO(Long tenantId, Long targetId, Integer kind, Long fieldId) {
        this.tenantId = tenantId;
        this.targetId = targetId;
        this.kind = kind;
        this.fieldId = fieldId;
    }

    public TargetPO() {
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    @JsonIgnore
    public String keyWithField() {
        return tenantId + "-" + targetId + "-" + fieldId;
    }

    @JsonIgnore
    public String keyWithKind() {
        return tenantId + "-" + targetId + "-" + kind;
    }

}
