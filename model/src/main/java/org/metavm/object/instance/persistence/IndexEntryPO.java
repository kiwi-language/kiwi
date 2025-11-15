package org.metavm.object.instance.persistence;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.util.EncodingUtils;
import org.metavm.util.Utils;

import java.util.Arrays;
import java.util.Objects;

public class IndexEntryPO implements Comparable<IndexEntryPO> {

    @Setter
    @Getter
    private long appId;
    @Getter
    private final IndexKeyPO key;
    @Setter
    @Getter
    private byte[] instanceId;
    private transient int hash;
    private transient boolean hashIsZero;

    public IndexEntryPO() {
        key = new IndexKeyPO();
    }

    public IndexEntryPO(long appId, IndexKeyPO key, byte[] instanceId) {
        this.appId = appId;
        this.key = key;
        this.instanceId = instanceId;
    }

    public byte[] getIndexId() {
        return key.getIndexId();
    }

    public void setIndexId(byte[] indexId) {
        key.setIndexId(indexId);
    }

    public byte[] getData() {
        return key.getData();
    }

    public void setData(byte[] data) {
        key.setData(data);
    }

    public Id getId() {
        return Id.fromBytes(instanceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntryPO that = (IndexEntryPO) o;
        return Objects.equals(key, that.key) && Arrays.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if(h == 0 && !hashIsZero) {
            h = Objects.hash(key, Arrays.hashCode(instanceId));
            if(h == 0)
                hashIsZero = true;
            else
                hash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        return "{\"appId\": " + appId + ", \"instanceId\": \"" + EncodingUtils.bytesToHex(instanceId) + "\", \"key\": " + key + "}";
    }

    public IndexEntryPO copy() {
        var copy = new IndexEntryPO(appId, key.copy(), instanceId);
        copy.hash = hash;
        copy.hashIsZero = hashIsZero;
        return copy;
    }

    @Override
    public int compareTo(@NotNull IndexEntryPO o) {
        if(appId != o.appId)
            return Long.compare(appId, o.appId);
        var keyComparison = key.compareTo(o.key);
        if (keyComparison != 0)
            return keyComparison;
        return Utils.compareBytes(instanceId, o.instanceId);
    }

}
