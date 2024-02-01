package tech.metavm.object.instance.persistence;

import java.util.Arrays;
import java.util.Objects;

public class IndexKeyPO {

    public static final int MAX_KEY_COLUMNS = 5;
    public static final byte[] NULL = {'\0'};

    private long indexId;
    private final byte[][] columns = new byte[MAX_KEY_COLUMNS][];

    public IndexKeyPO() {
        fillNull();
    }

    public void fillNull() {
        Arrays.fill(columns, NULL);
    }

    public long getIndexId() {
        return indexId;
    }

    public void setIndexId(long indexId) {
        this.indexId = indexId;
    }

    public byte[] getColumn0() {
        return columns[0];
    }

    public void setColumn0(byte[] column1) {
        columns[0] = column1;
    }

    public byte[] getColumn1() {
        return columns[1];
    }

    public void setColumn1(byte[] column1) {
        columns[1] = column1;
    }

    public byte[] getColumn2() {
        return columns[2];
    }

    public void setColumn2(byte[] column2) {
        columns[2] = column2;
    }

    public byte[] getColumn3() {
        return columns[3];
    }

    public void setColumn3(byte[] column3) {
        columns[3] = column3;
    }

    public byte[] getColumn4() {
        return columns[4];
    }

    public void setColumn4(byte[] column4) {
        columns[4] = column4;
    }

    public void setColumn(int i, byte[] column) {
        columns[i] = column;
    }

    public byte[] getColumn(int i) {
        return columns[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKeyPO that = (IndexKeyPO) o;
        return indexId == that.indexId && Arrays.deepEquals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(indexId);
        result = 31 * result + Arrays.deepHashCode(columns);
        return result;
    }

    @Override
    public String toString() {
        return "IndexKeyPO{" +
                "constraintId=" + indexId +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }

    public IndexKeyPO copy() {
        var copy = new IndexKeyPO();
        copy.indexId = indexId;
        for (int i = 0; i < MAX_KEY_COLUMNS; i++) {
            copy.columns[i] = Arrays.copyOf(columns[i], columns[i].length);
        }
        return copy;
    }
}
