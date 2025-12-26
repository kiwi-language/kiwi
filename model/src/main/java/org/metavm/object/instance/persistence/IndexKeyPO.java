package org.metavm.object.instance.persistence;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.BytesUtils;
import org.metavm.util.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class IndexKeyPO implements Comparable<IndexKeyPO> {

    public static final int MAX_KEY_COLUMNS = 15;
    public static final byte[] NULL = {'\0'};

    private byte[] indexId;
    private byte[] data;
    private transient int hash;
    private transient boolean hashIsZero;
    private List<byte[]> columns;

    public IndexKeyPO() {
    }

    public IndexKeyPO(byte[] indexId, byte[] data) {
        this.indexId = indexId;
        this.data = data;
    }

    public byte[] getIndexId() {
        return indexId;
    }

    public void setIndexId(byte[] indexId) {
        this.indexId = indexId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        columns = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKeyPO that = (IndexKeyPO) o;
        return Arrays.equals(indexId, that.indexId) && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            h = 31 * Arrays.hashCode(indexId) + Arrays.hashCode(data);
            if (h == 0)
                hashIsZero = true;
            else
                hash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        var indexIdStr = Id.fromBytes(indexId);
        var fields = Utils.map(getColumns(), BytesUtils::readIndexBytes);
        return "{\"indexId: \"" + indexIdStr + "\", \"fields\": [" + Utils.join(fields, Objects::toString) + "]}";
    }

    public List<Value> getColumnValues(Function<Id, Instance> resolver) {
        return Utils.map(getColumns(), b -> BytesUtils.readIndexValue(b, resolver));
    }

    public static List<Object> parseBytes(byte[] bytes) {
        return Utils.map(toColumns(bytes), BytesUtils::readIndexBytes);
    }

    List<byte[]> getColumns() {
        if(columns == null)
            columns = toColumns(data);
        return columns;
    }

    public static List<byte[]> toColumns(byte[] data) {
        if(data.length <= 1)
            return List.of();
        var bout = new ByteArrayOutputStream();
        var fieldBytes = new ArrayList<byte[]>();
        for (int i = 1; i < data.length; i++) {
            var b = data[i];
            if(b == (byte) 0x00){
              fieldBytes.add(bout.toByteArray());
              bout.reset();
            } else {
                if (b == (byte) 0xff) {
                    b = data[++i];
                    if (b == 0x00)
                        bout.write(0xfe);
                    else if (b == 0x01)
                        bout.write(0xff);
                    else
                        throw new IllegalStateException("Corrupted stream");
                }
                else
                    bout.write(b - 1);
            }
        }
        fieldBytes.add(bout.toByteArray());
        return fieldBytes;
    }

    public IndexKeyPO copy() {
        var copy = new IndexKeyPO(indexId, data);
        copy.hash = hash;
        copy.hashIsZero = hashIsZero;
        return copy;
    }

    @Override
    public int compareTo(@NotNull IndexKeyPO o) {
        var indexCmpResult = Utils.compareBytes(indexId, o.indexId);
        if (indexCmpResult != 0)
            return indexCmpResult;
        return Utils.compareBytes(data, o.data);
    }

}
