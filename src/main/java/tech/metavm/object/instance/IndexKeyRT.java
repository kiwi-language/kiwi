package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.Index;
import tech.metavm.object.meta.IndexField;
import tech.metavm.util.NncUtils;

import java.util.*;

public class IndexKeyRT {

    private final Index index;
    private final Map<IndexField, Instance> fields;

    public IndexKeyRT(Index index, Map<IndexField, Instance> fields) {
        this.index = index;
        this.fields = new HashMap<>(fields);
    }

    public Index getIndex() {
        return index;
    }

    public Map<IndexField, Instance> getFields() {
        return fields;
    }

    public IndexKeyPO toPO() {
        IndexKeyPO key = new IndexKeyPO();
        key.setConstraintId(index.getIdRequired());
        for (IndexField field : index.getFields()) {
            field.setKeyItem(key, fields.get(field));
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexKeyRT that)) return false;
        return Objects.equals(index, that.index) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, fields);
    }
}
