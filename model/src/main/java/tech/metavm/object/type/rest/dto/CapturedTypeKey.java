package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.CapturedType;
import tech.metavm.object.type.CapturedTypeVariable;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;

public record CapturedTypeKey(String variableId) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.writeId(Id.parse(variableId));
    }

    @Override
    public String toTypeExpression() {
        return String.format("#$$%s", variableId);
    }

    @Override
    public CapturedType toType(TypeDefProvider typeDefProvider) {
        return new CapturedType((CapturedTypeVariable) typeDefProvider.getTypeDef(Id.parse(variableId)));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitCapturedTypeKey(this);
    }

    @Override
    public void acceptChildren(TypeKeyVisitor<?> visitor) {

    }
}
