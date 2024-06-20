package org.metavm.object.type.rest.dto;

import org.metavm.object.type.AnyType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceOutput;

public record AnyTypeKey() implements TypeKey {

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.ANY);
    }

    @Override
    public String toTypeExpression() {
        return "any";
    }

    @Override
    public AnyType toType(TypeDefProvider typeDefProvider) {
        return AnyType.instance;
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitAnyTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.ANY;
    }

}
