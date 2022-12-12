package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;

public class BooleanInstance extends PrimitiveInstance {

    private final boolean value;

    public BooleanInstance(boolean value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BooleanInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return value ? "是" : "否";
    }
}
