package tech.metavm.object.instance.persistence;

import java.util.List;
import java.util.Objects;

public class IndexItemPO {

    private long tenantId;
    private final IndexKeyPO key;
    private long instanceId;

    public IndexItemPO(long tenantId, long constraintId, List<String> columns, long instanceId) {
        this.tenantId = tenantId;
        key = new IndexKeyPO(constraintId, columns);
        this.instanceId = instanceId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public IndexItemPO() {
        key = new IndexKeyPO();
    }

    public long getConstraintId() {
        return key.getConstraintId();
    }

    public void setConstraintId(long constraintId) {
        key.setConstraintId(constraintId);
    }

    public void setColumns(List<String> keys) {
        key.setColumns(keys);
    }

    public String getColumn1() {
        return key.getColumn1();
    }

    public void setColumn1(String column1) {
        key.setColumn1(column1);
    }

    public String getColumn2() {
        return key.getColumn2();
    }

    public void setColumn2(String column2) {
        key.setColumn2(column2);
    }

    public String getColumn3() {
        return key.getColumn3();
    }

    public void setColumn3(String column3) {
        key.setColumn3(column3);
    }

    public String getColumn4() {
        return key.getColumn4();
    }

    public void setColumn4(String column4) {
        key.setColumn4(column4);
    }

    public String getColumn5() {
        return key.getColumn5();
    }

    public void setColumn5(String column5) {
        key.setColumn5(column5);
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
        IndexItemPO that = (IndexItemPO) o;
        return instanceId == that.instanceId && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, instanceId);
    }
}
