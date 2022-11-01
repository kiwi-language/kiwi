package tech.metavm.object.instance.persistence;

import tech.metavm.util.InternalException;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IndexKeyPO {

    public static final int MAX_KEY_ITEMS = 3;

    private long constraintId;
    private String column1;
    private String column2;
    private String column3;

    public IndexKeyPO() {
        column1 = column2 = column3 = "\0";
    }

    public IndexKeyPO(long constraintId, List<String> columns) {
        this.constraintId = constraintId;
        setColumns(columns);
    }

    public void setColumns(List<String> keys) {
        if(keys.size() > MAX_KEY_ITEMS) {
            throw new InternalException("Key size exceeds maximum(" + MAX_KEY_ITEMS + ")");
        }
        Iterator<String> it = keys.iterator();
        column1 = column2 = column3 = "\0";
        if(it.hasNext()) {
            column1 = it.next();
        }
        if(it.hasNext()) {
            column2 = it.next();
        }
        if(it.hasNext()) {
            column3 = it.next();
        }
    }

    public long getConstraintId() {
        return constraintId;
    }

    public void setConstraintId(long constraintId) {
        this.constraintId = constraintId;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    private String getEffectiveColumn(String col) {
        return col != null ? col.replace("\0", "\0\0") : "\0";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKeyPO that = (IndexKeyPO) o;
        return Objects.equals(column1, that.column1) && Objects.equals(column2, that.column2) && Objects.equals(column3, that.column3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column1, column2, column3);
    }
}
