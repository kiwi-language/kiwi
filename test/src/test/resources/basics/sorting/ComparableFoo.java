package sorting;

import org.jetbrains.annotations.NotNull;

public class ComparableFoo implements Comparable<ComparableFoo> {

    private final int seq;

    public ComparableFoo(int seq) {
        this.seq = seq;
    }

    @Override
    public int compareTo(@NotNull ComparableFoo o) {
        return Integer.compare(seq, o.seq);
    }
}
