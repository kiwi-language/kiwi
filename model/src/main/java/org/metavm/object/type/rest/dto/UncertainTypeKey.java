package org.metavm.object.type.rest.dto;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.UncertainType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record UncertainTypeKey(TypeKey lowerBoundKey, TypeKey upperBoundKey) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNCERTAIN_TYPE);
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
        return WireTypes.UNCERTAIN_TYPE;
    }
}
