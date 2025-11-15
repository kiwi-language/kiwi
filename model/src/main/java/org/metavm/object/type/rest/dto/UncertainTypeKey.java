package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json
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
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitUncertainTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.UNCERTAIN_TYPE;
    }
}
