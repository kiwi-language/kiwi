package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexField;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassBuilder;
import org.metavm.util.BytesUtils;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IndexKeyRT implements Comparable<IndexKeyRT> {

    public static final Logger logger = LoggerFactory.getLogger(IndexKeyRT.class);

    private static final Klass DUMMY_TYPE = KlassBuilder.newBuilder("Dummy", "Dummy").build();

    public  static final Reference MIN_INSTANCE;

    public static final Reference MAX_INSTANCE;

    static {
        var i1 = ClassInstance.allocate(DUMMY_TYPE.getType());
        i1.initId(new NullId());
        MIN_INSTANCE = new Reference(i1);
        var i2 = ClassInstance.allocate(DUMMY_TYPE.getType());
        i2.initId(new MockId(Long.MAX_VALUE));
        i2.setSeq(Integer.MAX_VALUE);
        MAX_INSTANCE = new Reference(i2);
    }

    private final Index index;
    private final Map<IndexField, Value> fields;

    public IndexKeyRT(Index index, Map<IndexField, Value> fields) {
        this.index = index;
        this.fields = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            var indexField = index.getFields().get(i);
            this.fields.put(
                    indexField,
                    Objects.requireNonNull(fields.get(indexField),
                            () -> "Not an index prefix")
            );
        }
    }

    public IndexKeyPO toPO() {
        IndexKeyPO key = new IndexKeyPO();
        var index = getIndex();
        key.setIndexId(index.getId().toBytes());
        key.setData(getKeyBytes());
        return key;
    }

    public Index getIndex() {
        return index;
    }

    public Map<IndexField, Value> getFields() {
        return fields;
    }

    public Value getField(IndexField field) {
        return fields.get(field);
    }

    public InstanceIndexQuery toQuery() {
        var key = new InstanceIndexKey(index, Utils.map(index.getFields(), fields::get));
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
        if(index != that.index)
            return index.getId().compareTo(that.index.getId());
        for (int i = 0; i < index.getFields().size(); i++) {
            var field = index.getFields().get(i);
            var cmp = compare(fields.get(field), that.fields.get(field));
            if(cmp != 0)
                return cmp;
        }
        return 0;
    }

    private int compare(Value first, Value second) {
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
        if(first instanceof PrimitiveValue p1 && second instanceof PrimitiveValue p2)
            return p1.compareTo(p2);
        if(first instanceof NullValue)
            return -1;
        if(second instanceof NullValue)
            return 1;
        if(first instanceof Reference d1
                && second instanceof Reference d2) {
            if(d1.isNew() && d2.isNew())
                return Integer.compare(d1.get().getSeq(), d2.get().getSeq());
            if(d1.isNew())
                return 1;
            else if(d2.isNew())
                return -1;
            return d1.getId().compareTo(d2.getId());
        }
        throw new InternalException("Can not compare instances");
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("{index: ").append(index.getName()).append(", fields: {");
        sb.append(Utils.join(fields.entrySet(), e -> e.getKey().getName() + ": " + e.getValue().getText()));
        sb.append("}}");
        return sb.toString();
    }

    public byte[] getKeyBytes() {
        return toKeyBytes(Utils.map(index.getFields(), fields::get));
    }

    public static byte[] toKeyBytes(List<Value> values) {
        var bout = new ByteArrayOutputStream();
        for (var value : values) {
            var bytes = BytesUtils.toIndexBytes(value);
            bout.write(0x00);
            for (byte b : bytes) {
                if(b == (byte) 0xfe) {
                    bout.write(0xff);
                    bout.write(0x00);
                }
                else if(b == (byte) 0xff) {
                    bout.write(0xff);
                    bout.write(0x01);
                }
                else
                    bout.write(b + 1);
            }
        }
        return bout.toByteArray();
    }

}
