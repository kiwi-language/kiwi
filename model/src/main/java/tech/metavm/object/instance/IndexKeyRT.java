package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexField;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IndexKeyRT {

    private final Index index;
    private final Map<IndexField, Instance> fields;

    public IndexKeyRT(Index index, Map<IndexField, Instance> fields) {
        this.index = index;
        this.fields = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            var indexField = index.getFields().get(i);
            this.fields.put(
                    indexField,
                    NncUtils.requireNonNull(fields.get(indexField),
                            () -> new InternalException("Not an index prefix"))
            );
        }
    }

    public Index getIndex() {
        return index;
    }

    public Map<IndexField, Instance> getFields() {
        return fields;
    }

    public Instance getField(IndexField field) {
        return fields.get(field);
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
