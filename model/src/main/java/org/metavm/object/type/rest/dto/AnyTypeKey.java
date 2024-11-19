package org.metavm.object.type.rest.dto;

import org.metavm.object.type.AnyType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record AnyTypeKey() implements TypeKey {

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.ANY_TYPE);
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
        return WireTypes.ANY_TYPE;
    }

}
