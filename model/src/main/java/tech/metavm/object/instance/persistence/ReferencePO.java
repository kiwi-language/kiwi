package tech.metavm.object.instance.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.EncodingUtils;

import java.util.Arrays;
import java.util.Objects;

public class ReferencePO {
    private long appId;
    private long sourceTreeId;
    private byte[] targetId;
    private int kind;

    public ReferencePO(long appId,
                       long sourceTreeId,
                       byte[] targetId,
                       int kind) {
        this.appId = appId;
        this.sourceTreeId = sourceTreeId;
        this.targetId = targetId;
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

    public long getSourceTreeId() {
        return sourceTreeId;
    }

    public void setSourceTreeId(long sourceTreeId) {
        this.sourceTreeId = sourceTreeId;
    }

    public byte[] getTargetId() {
        return targetId;
    }

    public void setTargetId(byte[] targetId) {
        this.targetId = targetId;
    }

    public Id getTargetInstanceId() {
        return Id.fromBytes(targetId);
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
                && sourceTreeId == that.sourceTreeId
                && Arrays.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, sourceTreeId, Arrays.hashCode(targetId));
    }

    @Override
    public String toString() {
        return "ReferencePO{" +
                "appId=" + appId +
                ", sourceId=" + sourceTreeId +
                ", targetId=" + EncodingUtils.bytesToHex(targetId) +
                ", kind=" + kind +
                '}';
    }

    @JsonIgnore
    public String targetKeyWithKind() {
        return appId + "-" + EncodingUtils.bytesToHex(targetId) + "-" + kind;
    }

    @JsonIgnore
    public String targetKeyWithField() {
        return appId + "-" + EncodingUtils.bytesToHex(targetId);
    }

}
