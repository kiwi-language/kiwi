package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.type.Index;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassBuilder;
import org.metavm.util.BytesUtils;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IndexKeyRT implements Comparable<IndexKeyRT> {

    public static final Logger logger = LoggerFactory.getLogger(IndexKeyRT.class);

    private static final Klass DUMMY_TYPE = KlassBuilder.newBuilder(new NullId(), "Dummy", "Dummy").ephemeral(true).build();

    public  static final Reference MIN_INSTANCE;

    public static final Reference MAX_INSTANCE;

    static {
        var i1 = ClassInstance.allocate(new NullId(), DUMMY_TYPE.getType());
        MIN_INSTANCE = new Reference(i1);
        var i2 = ClassInstance.allocate(new MockId(Long.MAX_VALUE), DUMMY_TYPE.getType());
        i2.setSeq(Integer.MAX_VALUE);
        MAX_INSTANCE = new Reference(i2);
    }

    private final Index index;
    private final List<Value> values;

    public IndexKeyRT(Index index, List<Value> values) {
        this.index = index;
        this.values = new ArrayList<>(values);
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

    public List<Value> getValues() {
        return values;
    }


    public InstanceIndexQuery toQuery() {
        var key = new InstanceIndexKey(index, values);
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
        return Objects.equals(index, that.index) && Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, values);
    }

    public int compareTo(IndexKeyRT that) {
        if(index != that.index)
            return index.getId().compareTo(that.index.getId());
        var it = that.values.iterator();
        for (Value value : values) {
            var cmp = compare(value, it.next());
            if (cmp != 0) return cmp;
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
        if (first instanceof StringReference s1 && second instanceof StringReference s2)
            return s1.compareTo(s2);
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
        sb.append(Utils.join(values, Value::getText));
        sb.append("}}");
        return sb.toString();
    }

    public byte[] getKeyBytes() {
        return toKeyBytes(values);
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
