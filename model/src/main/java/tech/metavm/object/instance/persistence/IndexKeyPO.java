package tech.metavm.object.instance.persistence;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;

import java.util.Arrays;

public class IndexKeyPO implements Comparable<IndexKeyPO> {

    public static final int MAX_KEY_COLUMNS = 15;
    public static final byte[] NULL = {'\0'};

    private byte[] indexId;
    private final byte[][] columns = new byte[MAX_KEY_COLUMNS][];

    public IndexKeyPO() {
        fillNull();
    }

    public IndexKeyPO(byte[] indexId, byte[][] columns) {
        this.indexId = indexId;
        for (int i = 0; i < MAX_KEY_COLUMNS; i++) {
            this.columns[i] = Arrays.copyOf(columns[i], columns[i].length);
        }
    }

    public void fillNull() {
        Arrays.fill(columns, NULL);
    }

    public byte[] getIndexId() {
        return indexId;
    }

    public void setIndexId(byte[] indexId) {
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

    public byte[] getColumn5() {
        return columns[5];
    }

    public void setColumn5(byte[] column5) {
        columns[5] = column5;
    }

    public byte[] getColumn6() {
        return columns[6];
    }

    public void setColumn6(byte[] column6) {
        columns[6] = column6;
    }

    public byte[] getColumn7() {
        return columns[7];
    }

    public void setColumn7(byte[] column7) {
        columns[7] = column7;
    }

    public byte[] getColumn8() {
        return columns[8];
    }

    public void setColumn8(byte[] column8) {
        columns[8] = column8;
    }

    public byte[] getColumn9() {
        return columns[9];
    }

    public void setColumn9(byte[] column9) {
        columns[9] = column9;
    }

    public byte[] getColumn10() {
        return columns[10];
    }

    public void setColumn10(byte[] column10) {
        columns[10] = column10;
    }

    public byte[] getColumn11() {
        return columns[11];
    }

    public void setColumn11(byte[] column11) {
        columns[11] = column11;
    }

    public byte[] getColumn12() {
        return columns[12];
    }

    public void setColumn12(byte[] column12) {
        columns[12] = column12;
    }

    public byte[] getColumn13() {
        return columns[13];
    }

    public void setColumn13(byte[] column13) {
        columns[13] = column13;
    }

    public byte[] getColumn14() {
        return columns[14];
    }

    public void setColumn14(byte[] column14) {
        columns[14] = column14;
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
        return Arrays.equals(indexId, that.indexId) && Arrays.deepEquals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(indexId);
        result = 31 * result + Arrays.deepHashCode(columns);
        return result;
    }

    @Override
    public String toString() {
        return "IndexKeyPO{" +
                "indexId=" + Id.fromBytes(indexId) +
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


    @Override
    public int compareTo(@NotNull IndexKeyPO o) {
        if(!Arrays.equals(indexId, o.indexId))
            throw new RuntimeException("Can not compare keys from different indexes");
        for (int i = 0; i < MAX_KEY_COLUMNS; i++) {
            var cmp = UnsignedBytes.lexicographicalComparator().compare(columns[i], o.columns[i]);
            if(cmp != 0)
                return cmp;
        }
        return 0;
    }

}
