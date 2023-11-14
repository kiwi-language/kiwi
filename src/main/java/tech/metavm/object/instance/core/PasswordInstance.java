package tech.metavm.object.instance.core;

import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.meta.PrimitiveKind;
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
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                "******",
                PrimitiveKind.PASSWORD.getCode(),
                null
        );
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitPasswordInstance(this);
    }
}
