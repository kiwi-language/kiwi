package tech.metavm.object.type.rest.dto;

import tech.metavm.object.type.AnyType;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;

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
        return new AnyType();
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitAnyTypeKey(this);
    }

    @Override
    public void acceptChildren(TypeKeyVisitor<?> visitor) {

    }

}
