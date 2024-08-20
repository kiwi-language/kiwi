package org.metavm.object.instance.persistence;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.util.EncodingUtils;

import java.util.Arrays;
import java.util.Objects;

public class IndexEntryPO implements Comparable<IndexEntryPO> {

    private long appId;
    private final IndexKeyPO key;
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

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
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

    public IndexKeyPO getKey() {
        return key;
    }

    public byte[] getInstanceId() {
        return instanceId;
    }

    public Id getId() {
        return Id.fromBytes(instanceId);
    }

    public void setInstanceId(byte[] instanceId) {
        this.instanceId = instanceId;
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
        return UnsignedBytes.lexicographicalComparator().compare(instanceId, o.instanceId);
    }
}
