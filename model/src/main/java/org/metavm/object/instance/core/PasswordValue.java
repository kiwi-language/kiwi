package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class PasswordValue extends PrimitiveValue {

    private final String value;

    public PasswordValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.passwordType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.PASSWORD);
        output.writeUTF(value);
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
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitPasswordValue(this);
    }
}
