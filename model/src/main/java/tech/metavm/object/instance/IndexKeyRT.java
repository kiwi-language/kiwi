package tech.metavm.object.instance;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexField;
import tech.metavm.util.BytesUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IndexKeyRT implements Comparable<IndexKeyRT> {

    private static final ClassType DUMMY_TYPE = ClassTypeBuilder.newBuilder("Dummy", "Dummy").build();

    public  static final ClassInstance MIN_INSTANCE = ClassInstance.allocate(DUMMY_TYPE);

    public static final ClassInstance MAX_INSTANCE = ClassInstance.allocate(DUMMY_TYPE);

    static {
        MIN_INSTANCE.initId(new NullId());
        MAX_INSTANCE.initId(new MockId(Long.MAX_VALUE));
        MAX_INSTANCE.setSeq(Integer.MAX_VALUE);
    }

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

    public IndexKeyPO toPO() {
        IndexKeyPO key = new IndexKeyPO();
        var index = getIndex();
        key.setIndexId(index.getId().toBytes());
        for (IndexField field : index.getFields()) {
            var fieldValue = getFields().get(field);
            if(fieldValue != null)
                setKeyItem(field, key, fieldValue);
        }
        return key;
    }

    private static void setKeyItem(IndexField field, IndexKeyPO key, Instance fieldValue) {
        key.setColumn(field.getIndex().getFieldIndex(field), BytesUtils.toIndexBytes(fieldValue));
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

    public InstanceIndexQuery toQuery() {
        var key = new InstanceIndexKey(index, NncUtils.map(index.getFields(), fields::get));
        return new InstanceIndexQuery(
                index,
                key,
                key,
                false,
                null
        );
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

    public int compareTo(IndexKeyRT that) {
        var indexComparison = index.getId().compareTo(that.index.getId());
        if(indexComparison != 0)
            return indexComparison;
        for (int i = 0; i < index.getFields().size(); i++) {
            var field = index.getFields().get(i);
            var cmp = compare(fields.get(field), that.fields.get(field));
            if(cmp != 0)
                return cmp;
        }
        return 0;
    }

    private int compare(Instance first, Instance second) {
        if(first == second)
            return 0;
        if(first == MIN_INSTANCE)
            return -1;
        if(second == MIN_INSTANCE)
            return 1;
        if(first == MAX_INSTANCE)
            return 1;
        if(second == MAX_INSTANCE)
            return -1;
        if(first instanceof PrimitiveInstance p1 && second instanceof PrimitiveInstance p2)
            return p1.compareTo(p2);
        if(first instanceof NullInstance)
            return -1;
        if(second instanceof NullInstance)
            return 1;
        if(first instanceof DurableInstance d1 && !d1.isView()
                && second instanceof DurableInstance d2 && !d2.isView()) {
            if(d1.isNew() && d2.isNew())
                return Integer.compare(d1.getSeq(), d2.getSeq());
            if(d1.isNew())
                return 1;
            else if(d2.isNew())
                return -1;
            return d1.getId().compareTo(d2.getId());
        }
        throw new InternalException("Can not compare instances");
    }

}
