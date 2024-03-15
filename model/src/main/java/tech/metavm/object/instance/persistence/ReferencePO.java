package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;

import java.util.Objects;

public class ReferencePO {
    private long appId;
    private int sourceTypeTag;
    private long sourceTypeId;
    private long sourceId;
    private int targetTypeTag;
    private long targetTypeId;
    private long targetId;
    private long fieldId;
    private int kind;

    public ReferencePO(long appId,
                       long sourceId, int sourceTypeTag,
                       long sourceTypeId,
                       long targetId, int targetTypeTag,
                       long targetTypeId,
                       long fieldId,
                       int kind) {
        this.appId = appId;
        this.sourceTypeTag = sourceTypeTag;
        this.sourceTypeId = sourceTypeId;
        this.sourceId = sourceId;
        this.targetTypeTag = targetTypeTag;
        this.targetTypeId = targetTypeId;
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

    public int getSourceTypeTag() {
        return sourceTypeTag;
    }

    public void setSourceTypeTag(int sourceTypeTag) {
        this.sourceTypeTag = sourceTypeTag;
    }

    public long getSourceTypeId() {
        return sourceTypeId;
    }

    public void setSourceTypeId(long sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public Id getSourceInstanceId() {
        return PhysicalId.of(sourceId, TypeTag.fromCode(sourceTypeTag), sourceTypeId);
    }

    public int getTargetTypeTag() {
        return targetTypeTag;
    }

    public void setTargetTypeTag(int targetTypeTag) {
        this.targetTypeTag = targetTypeTag;
    }

    public long getTargetTypeId() {
        return targetTypeId;
    }

    public void setTargetTypeId(long targetTypeId) {
        this.targetTypeId = targetTypeId;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public Id getTargetInstanceId() {
        return PhysicalId.of(targetId, TypeTag.fromCode(targetTypeTag), targetTypeId);
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
