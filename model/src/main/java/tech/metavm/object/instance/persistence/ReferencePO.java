package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.EncodingUtils;

import java.util.Arrays;
import java.util.Objects;

public class ReferencePO {
    private long appId;
    private byte[] sourceId;
    private byte[] targetId;
    private byte[] fieldId;
    private int kind;

    public ReferencePO(long appId,
                       byte[] sourceId,
                       byte[] targetId,
                       byte[] fieldId,
                       int kind) {
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

    public byte[] getSourceId() {
        return sourceId;
    }

    public void setSourceId(byte[] sourceId) {
        this.sourceId = sourceId;
    }

    public byte[] getTargetId() {
        return targetId;
    }

    public void setTargetId(byte[] targetId) {
        this.targetId = targetId;
    }

    public Id getSourceInstanceId() {
        return Id.fromBytes(sourceId);
    }

    public Id getTargetInstanceId() {
        return Id.fromBytes(targetId);
    }

    public byte[] getFieldId() {
        return fieldId;
    }

    public void setFieldId(byte[] fieldId) {
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
        return appId == that.appId
                && Arrays.equals(sourceId, that.sourceId)
                && Arrays.equals(targetId, that.targetId)
                && Arrays.equals(fieldId, that.fieldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, Arrays.hashCode(sourceId), Arrays.hashCode(targetId), Arrays.hashCode(fieldId));
    }

    @Override
    public String toString() {
        return "ReferencePO{" +
                "appId=" + appId +
                ", sourceId=" + EncodingUtils.bytesToHex(sourceId) +
                ", targetId=" + EncodingUtils.bytesToHex(targetId) +
                ", fieldId=" + (fieldId != null ? EncodingUtils.bytesToHex(fieldId) : "null") +
                ", kind=" + kind +
                '}';
    }

    @JsonIgnore
    public String targetKeyWithKind() {
        return appId + "-" + EncodingUtils.bytesToHex(targetId) + "-" + kind;
    }

    @JsonIgnore
    public String targetKeyWithField() {
        if (fieldId != null)
            return appId + "-" + EncodingUtils.bytesToHex(targetId) + "-" + EncodingUtils.bytesToHex(fieldId);
        else
            return appId + "-" + EncodingUtils.bytesToHex(targetId);
    }

}
