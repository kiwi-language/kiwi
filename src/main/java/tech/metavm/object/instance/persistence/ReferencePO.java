package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class ReferencePO {
    private long tenantId;
    private long sourceId;
    private long targetId;
    private long fieldId;
    private int kind;

    public ReferencePO(long tenantId, long sourceId, long targetId, long fieldId, int kind) {
        this.tenantId = tenantId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.fieldId = fieldId;
        this.kind = kind;
    }

    public ReferencePO() {
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferencePO that = (ReferencePO) o;
        return tenantId == that.tenantId && sourceId == that.sourceId && targetId == that.targetId && fieldId == that.fieldId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, sourceId, targetId, fieldId);
    }

    @Override
    public String toString() {
        return "ReferencePO{" +
                "tenantId=" + tenantId +
                ", sourceId=" + sourceId +
                ", targetId=" + targetId +
                ", fieldId=" + fieldId +
                ", kind=" + kind +
                '}';
    }

    @JsonIgnore
    public String targetKeyWithKind() {
        return tenantId + "-" + targetId + "-" + kind;
    }

    @JsonIgnore
    public String targetKeyWithField() {
        return tenantId + "-" + targetId + "-" + fieldId;
    }

    public static Long convertToRefId(Object fieldValue, boolean isRef) {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue instanceof IdentityPO identityPO) {
            return identityPO.id();
        }
        if (isRef) {
            return (Long) fieldValue;
        }
        return null;
    }

}
