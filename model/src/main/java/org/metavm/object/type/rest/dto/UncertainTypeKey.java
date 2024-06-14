package org.metavm.object.type.rest.dto;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.UncertainType;
import org.metavm.util.InstanceOutput;

public record UncertainTypeKey(TypeKey lowerBoundKey, TypeKey upperBoundKey) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.UNCERTAIN);
        lowerBoundKey.write(output);
        upperBoundKey.write(output);
    }

    @Override
    public String toTypeExpression() {
        return "[" + lowerBoundKey.toTypeExpression() + "," + upperBoundKey.toTypeExpression() + "]";
    }

    @Override
    public UncertainType toType(TypeDefProvider typeDefProvider) {
        return new UncertainType(lowerBoundKey.toType(typeDefProvider), upperBoundKey.toType(typeDefProvider));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitUncertainTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.UNCERTAIN;
    }
}
