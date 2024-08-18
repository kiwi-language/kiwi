package org.metavm.system;

import java.util.concurrent.atomic.AtomicLong;

public class Block {
    private final AtomicLong next;
    private final long max;

    public Block(long min, long max) {
        this.next = new AtomicLong(min);
        this.max = max;
    }

    public IdRange allocate(int count) {
        var min = Math.min(this.next.getAndAdd(count), max);
        return new IdRange(min, Math.min(min + count, max));
    }

    public boolean hasNext() {
        return next.get() < max;
    }

}
