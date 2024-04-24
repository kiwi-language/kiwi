package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.object.type.VariableType;
import tech.metavm.util.InstanceOutput;

public record VariableTypeKey(String variableId) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.VARIABLE);
        output.writeId(Id.parse(variableId));
    }

    @Override
    public String toTypeExpression() {
        return "$$" + variableId;
    }

    @Override
    public VariableType toType(TypeDefProvider typeDefProvider) {
        return new VariableType((TypeVariable) typeDefProvider.getTypeDef(Id.parse(variableId)));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitVariableTypeKey(this);
    }

    @Override
    public void acceptChildren(TypeKeyVisitor<?> visitor) {

    }
}
