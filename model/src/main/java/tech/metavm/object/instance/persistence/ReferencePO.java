package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class ReferencePO {
    private long appId;
    private long sourceId;
    private long targetId;
    private long fieldId;
    private int kind;

    public ReferencePO(long appId, long sourceId, long targetId, long fieldId, int kind) {
        this.appId = appId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.fieldId = fieldId;
        this.kind = kind;
    }

    public ReferencePO() {
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferencePO that = (ReferencePO) o;
        return appId == that.appId && sourceId == that.sourceId && targetId == that.targetId && fieldId == that.fieldId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, sourceId, targetId, fieldId);
    }

    @Override
    public String toString() {
        return "ReferencePO{" +
                "appId=" + appId +
                ", sourceId=" + sourceId +
                ", targetId=" + targetId +
                ", fieldId=" + fieldId +
                ", kind=" + kind +
                '}';
    }

    @JsonIgnore
    public String targetKeyWithKind() {
        return appId + "-" + targetId + "-" + kind;
    }

    @JsonIgnore
    public String targetKeyWithField() {
        return appId + "-" + targetId + "-" + fieldId;
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
