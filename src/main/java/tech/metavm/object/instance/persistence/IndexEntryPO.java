package tech.metavm.object.instance.persistence;

import java.util.Objects;

public class IndexEntryPO {

    private long tenantId;
    private final IndexKeyPO key;
    private long instanceId;

    public IndexEntryPO(long tenantId, IndexKeyPO key, long instanceId) {
        this.tenantId = tenantId;
        this.key = key;
        this.instanceId = instanceId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public IndexEntryPO() {
        key = new IndexKeyPO();
    }

    public long getConstraintId() {
        return key.getConstraintId();
    }

    public void setConstraintId(long constraintId) {
        key.setConstraintId(constraintId);
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

    public void setColumnX(Long columnX) {
        key.setColumnX(columnX);
    }

    public boolean isColumnXPresent() {
        return key.isColumnXPresent();
    }

    public void setColumnXPresent(boolean columnXPresent) {
        key.setColumnXPresent(columnXPresent);
    }

    public byte[] getColumn(int i) {
        return key.getColumn(i);
    }

    public Long getColumnX() {
        return key.getColumnX();
    }

    public IndexKeyPO getKey() {
        return key;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntryPO that = (IndexEntryPO) o;
        return instanceId == that.instanceId && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, instanceId);
    }

    @Override
    public String toString() {
        return "IndexItemPO{" +
                "tenantId=" + tenantId +
                ", key=" + key +
                ", instanceId=" + instanceId +
                '}';
    }
}
