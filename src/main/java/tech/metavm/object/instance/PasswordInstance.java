package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.PrimitiveFieldValueDTO;
import tech.metavm.object.meta.PrimitiveType;

public class PasswordInstance extends PrimitiveInstance {

    private final String value;

    public PasswordInstance(String value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveFieldValueDTO toFieldValueDTO() {
        return new PrimitiveFieldValueDTO(
                "******", null
        );
    }

    @Override
    public String toString() {
        return "PasswordInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return value;
    }
}
