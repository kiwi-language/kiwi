package org.metavm.object.instance.core;

public class Refcount {
    private final Id target;
    private int count;

    public Refcount(Id target) {
        this.target = target;
    }

    public Refcount(Id target, int count) {
        this.target = target;
        this.count = count;
    }

    public Id getTarget() {
        return target;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void inc(int amount) {
        count += amount;
    }

    public void dec(int amount) {
        count -= amount;
    }

}
