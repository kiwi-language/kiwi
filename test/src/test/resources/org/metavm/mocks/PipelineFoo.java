package org.metavm.mocks;

import org.jetbrains.annotations.NotNull;

public class PipelineFoo<T> implements Comparable<PipelineFoo<T>> {

    private final int seq;

    public PipelineFoo(int seq) {
        this.seq = seq;
    }

    @Override
    public int compareTo(@NotNull PipelineFoo o) {
        return Integer.compare(seq, o.seq);
    }
}

class PipelineFooExt extends PipelineFoo<String> {

    public PipelineFooExt(int seq) {
        super(seq);
    }
}