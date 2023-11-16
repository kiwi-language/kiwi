package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceUtils;

public class LongInstance extends NumberInstance {

    private final long value;

    public LongInstance(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public Long toColumnValue() {
        return value;
    }

    public LongInstance inc(long inc) {
        return new LongInstance(value + inc, getType());
    }

    public LongInstance dec(long dec) {
        return new LongInstance(value - dec, getType());
    }

    public LongInstance minus(LongInstance that) {
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

    public BooleanInstance isGreaterThan(LongInstance that) {
        return InstanceUtils.createBoolean(value > that.value);
    }

    public BooleanInstance isGreaterThanOrEqualTo(LongInstance that) {
        return InstanceUtils.createBoolean(value >= that.value);
    }

    public BooleanInstance isLessThan(LongInstance that) {
        return InstanceUtils.createBoolean(value < that.value);
    }

    public BooleanInstance isLessThanOrEqualTo(LongInstance that) {
        return InstanceUtils.createBoolean(value <= that.value);
    }

    @Override
    public String getTitle() {
        return Long.toString(value);
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitLongInstance(this);
    }
}
