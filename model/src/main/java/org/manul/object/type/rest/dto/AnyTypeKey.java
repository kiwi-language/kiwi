package org.manul.object.type.rest.dto;

import org.jsonk.Json;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

@Json
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
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitAnyTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.ANY_TYPE;
    }

}
