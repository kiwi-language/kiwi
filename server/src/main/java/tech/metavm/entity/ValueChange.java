package tech.metavm.entity;

import java.util.ArrayList;
import java.util.List;

public class ValueChange<T extends Value> {

    private final Class<T> valueType;

    private final List<T> inserts = new ArrayList<>();
    private final List<T> deletes = new ArrayList<>();

    public ValueChange(Class<T> valueType) {
        this.valueType = valueType;
    }

    public void addInsert(T insert) {
        inserts.add(insert);
    }

    public void addDelete(T delete) {
        deletes.add(delete);
    }

    public List<T> getInserts() {
        return inserts;
    }

    public List<T> getDeletes() {
        return deletes;
    }

    public Class<T> getValueType() {
        return valueType;
    }

    public boolean isEmpty() {
        return inserts.isEmpty() && deletes.isEmpty();
    }

}
