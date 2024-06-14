package org.metavm.autograph;

import java.io.Closeable;

public abstract class State implements Closeable {

    private final StateStack stack;
    private final State parent;
    private final int level;

    public State(State parent, StateStack stack) {
        this.parent = parent;
        this.stack = stack;
        level = parent == null ? 0 : parent.level + 1;
    }

    public int level() {
        return level;
    }

    @Override
    public void close() {
        stack.pop(this.getClass());
    }

}
