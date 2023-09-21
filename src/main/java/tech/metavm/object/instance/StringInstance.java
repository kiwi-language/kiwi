package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

public class StringInstance extends PrimitiveInstance {

    private final String value;

    public StringInstance(String value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public BooleanInstance contains(StringInstance that) {
        return InstanceUtils.createBoolean(value.contains(that.value));
    }

    public BooleanInstance startsWith(StringInstance that) {
        return InstanceUtils.createBoolean(value.startsWith(that.value));
    }

    public StringInstance concat(StringInstance that) {
        return new StringInstance(value + that.value, getType());
    }

    public BooleanInstance isBlank() {
        return InstanceUtils.createBoolean(NncUtils.isBlank(value));
    }

    @Override
    public String toString() {
        return "StringInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return value;
    }

}
