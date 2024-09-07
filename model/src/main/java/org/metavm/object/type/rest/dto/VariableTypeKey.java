package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.VariableType;
import org.metavm.util.Constants;
import org.metavm.util.InstanceOutput;

public record VariableTypeKey(GenericDeclarationRefKey genericDeclarationRefKey, @NotNull Id rawVariableId) implements TypeKey {
    
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.VARIABLE);
        output.writeId(rawVariableId);
    }

    @Override
    public String toTypeExpression() {
        return genericDeclarationRefKey.toTypeExpression() + "@" + Constants.addIdPrefix(rawVariableId.toString());
    }

    @Override
    public VariableType toType(TypeDefProvider typeDefProvider) {
        return new VariableType(
                genericDeclarationRefKey.resolve(typeDefProvider),
                (TypeVariable) typeDefProvider.getTypeDef(rawVariableId)
        );
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitVariableTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.VARIABLE;
    }
}
