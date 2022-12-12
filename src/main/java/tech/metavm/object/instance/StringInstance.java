package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;

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

    @Override
    public String toString() {
        return "StringInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return value;
    }
}
