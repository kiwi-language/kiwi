package tech.metavm.object.instance.core;

import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.WireTypes;

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
    public void write(InstanceOutput output) {
        output.write(WireTypes.PASSWORD);
        output.writeString(value);
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                "******",
                PrimitiveKind.PASSWORD.code(),
                null
        );
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitPasswordInstance(this);
    }
}
