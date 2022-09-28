package tech.metavm.object.instance.persistence;

import java.util.Objects;

/**
    * 实例关系
    */
public class RelationPO {
    /**
    * 租户ID
    */
    private Long tenantId;

    /**
    * 属性ID
    */
    private Long fieldId;

    /**
    * 起点实例ID
    */
    private Long srcInstanceId;

    /**
    * 终点实例ID
    */
    private Long destInstanceId;

    public RelationPO() {
    }

    public RelationPO(Long tenantId, Long fieldId, Long srcInstanceId, Long destInstanceId) {
        this.tenantId = tenantId;
        this.fieldId = fieldId;
        this.srcInstanceId = srcInstanceId;
        this.destInstanceId = destInstanceId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getSrcInstanceId() {
        return srcInstanceId;
    }

    public void setSrcInstanceId(Long srcInstanceId) {
        this.srcInstanceId = srcInstanceId;
    }

    public Long getDestInstanceId() {
        return destInstanceId;
    }

    public void setDestInstanceId(Long destInstanceId) {
        this.destInstanceId = destInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationPO that = (RelationPO) o;
        return Objects.equals(tenantId, that.tenantId) && Objects.equals(fieldId, that.fieldId) && Objects.equals(srcInstanceId, that.srcInstanceId) && Objects.equals(destInstanceId, that.destInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, fieldId, srcInstanceId, destInstanceId);
    }
}