package tech.metavm.object.instance.persistence;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.EncodingUtils;

import java.util.Arrays;
import java.util.Objects;

public class IndexEntryPO implements Comparable<IndexEntryPO> {

    private long appId;
    private final IndexKeyPO key;
    private byte[] instanceId;
    private transient int hash;
    private transient boolean hashIsZero;

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

    public IndexEntryPO() {
        key = new IndexKeyPO();
    }

    public byte[] getIndexId() {
        return key.getIndexId();
    }

    public void setIndexId(byte[] indexId) {
        key.setIndexId(indexId);
    }

    public byte[] getColumn0() {
        return key.getColumn0();
    }

    public void setColumn0(byte[] column) {
        key.setColumn0(column);
    }

    public byte[] getColumn1() {
        return key.getColumn1();
    }

    public void setColumn1(byte[] column) {
        key.setColumn1(column);
    }

    public byte[] getColumn2() {
        return key.getColumn2();
    }

    public void setColumn2(byte[] column) {
        key.setColumn2(column);
    }

    public byte[] getColumn3() {
        return key.getColumn3();
    }

    public void setColumn3(byte[] column) {
        key.setColumn3(column);
    }

    public byte[] getColumn4() {
        return key.getColumn4();
    }

    public void setColumn4(byte[] column4) {
        key.setColumn4(column4);
    }

    public void setColumn5(byte[] column) {
        key.setColumn5(column);
    }

    public byte[] getColumn5() {
        return key.getColumn5();
    }

    public void setColumn6(byte[] column) {
        key.setColumn6(column);
    }

    public byte[] getColumn6() {
        return key.getColumn6();
    }

    public void setColumn7(byte[] column) {
        key.setColumn7(column);
    }

    public byte[] getColumn7() {
        return key.getColumn7();
    }

    public void setColumn8(byte[] column) {
        key.setColumn8(column);
    }

    public byte[] getColumn8() {
        return key.getColumn8();
    }

    public void setColumn9(byte[] column) {
        key.setColumn9(column);
    }

    public byte[] getColumn9() {
        return key.getColumn9();
    }

    public void setColumn10(byte[] column) {
        key.setColumn10(column);
    }

    public byte[] getColumn10() {
        return key.getColumn10();
    }

    public void setColumn11(byte[] column) {
        key.setColumn11(column);
    }

    public byte[] getColumn11() {
        return key.getColumn11();
    }

    public void setColumn12(byte[] column) {
        key.setColumn12(column);
    }

    public byte[] getColumn12() {
        return key.getColumn12();
    }

    public void setColumn13(byte[] column) {
        key.setColumn13(column);
    }

    public byte[] getColumn13() {
        return key.getColumn13();
    }

    public void setColumn14(byte[] column) {
        key.setColumn14(column);
    }

    public byte[] getColumn14() {
        return key.getColumn14();
    }

    public byte[] getColumn(int i) {
        return key.getColumn(i);
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
        return "IndexItemPO{" +
                "appId=" + appId +
                ", key=" + key +
                ", instanceId=" + EncodingUtils.bytesToHex(instanceId) +
                '}';
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
