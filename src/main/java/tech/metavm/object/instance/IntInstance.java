//package tech.metavm.object.instance;
//
//import tech.metavm.object.meta.PrimitiveType;
//import tech.metavm.util.InstanceUtils;
//
//public class IntInstance extends NumberInstance {
//
//    private final int value;
//
//    public IntInstance(int value, PrimitiveType type) {
//        super(type);
//        this.value = value;
//    }
//
//    @Override
//    public Integer getValue() {
//        return value;
//    }
//
//    public IntInstance inc(int inc) {
//        return new IntInstance(value + inc, getType());
//    }
//
//    public IntInstance dec(int dec) {
//        return new IntInstance(value - dec, getType());
//    }
//
//    public IntInstance minus(IntInstance that) {
//        return new IntInstance(value - that.value, getType());
//    }
//
//    public IntInstance add(IntInstance that) {
//        return new IntInstance(value + that.value, getType());
//    }
//
//    public IntInstance mul(IntInstance that) {
//        return new IntInstance(value * that.value, getType());
//    }
//
//    public IntInstance div(IntInstance that) {
//        return new IntInstance(value / that.value, getType());
//    }
//
//    public IntInstance mod(IntInstance that) {
//        return new IntInstance(value % that.value, getType());
//    }
//
//
//    public BooleanInstance isGreaterThan(IntInstance that) {
//        return InstanceUtils.createBoolean(value > that.value);
//    }
//
//    public BooleanInstance isGreaterThanOrEqualTo(IntInstance that) {
//        return InstanceUtils.createBoolean(value >= that.value);
//    }
//
//    public BooleanInstance isLessThan(IntInstance that) {
//        return InstanceUtils.createBoolean(value < that.value);
//    }
//
//    public BooleanInstance isLessThanOrEqualTo(IntInstance that) {
//        return InstanceUtils.createBoolean(value <= that.value);
//    }
//
//    @Override
//    public String getTitle() {
//        return Integer.toString(value);
//    }
//
//    @Override
//    public String toString() {
//        return "IntInstance " + value + ":" + getType().getName();
//    }
//}
