package bitset;

import org.metavm.api.ChildEntity;

public class BitSet {

    @ChildEntity
    private final long[] bits;

    public BitSet(int n) {
        this.bits = new long[((n - 1) >> 6) + 1];
    }

    public void setBit(int i) {
        bits[i >> 6] |= 1L << i;
    }

    public boolean isClear(int i) {
        return (bits[i >> 6] & (1L << i)) == 0;
    }
}
