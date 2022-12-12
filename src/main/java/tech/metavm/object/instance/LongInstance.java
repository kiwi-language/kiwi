package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;

public class LongInstance extends PrimitiveInstance {

    private final long value;

    public LongInstance(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public LongInstance inc(long inc) {
        return new LongInstance(value + inc, getType());
    }

    public LongInstance dec(long dec) {
        return new LongInstance(value - dec, getType());
    }

    public LongInstance subtract(LongInstance that) {
        return new LongInstance(value - that.value, getType());
    }

    public LongInstance add(LongInstance that) {
        return new LongInstance(value + that.value, getType());
    }

    public LongInstance mul(LongInstance that) {
        return new LongInstance(value * that.value, getType());
    }

    public LongInstance div(LongInstance that) {
        return new LongInstance(value / that.value, getType());
    }

    public LongInstance mod(LongInstance that) {
        return new LongInstance(value % that.value, getType());
    }

    @Override
    public String toString() {
        return "LongInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return Long.toString(value);
    }
}
