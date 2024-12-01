package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.VariableType;
import org.metavm.util.Constants;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record VariableTypeKey(@NotNull Id variableId) implements TypeKey {
    
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.VARIABLE_TYPE);
        output.writeId(variableId);
    }

    @Override
    public String toTypeExpression() {
        return "@" + Constants.addIdPrefix(variableId.toString());
    }

    @Override
    public VariableType toType(TypeDefProvider typeDefProvider) {
        return new VariableType(
                (TypeVariable) typeDefProvider.getTypeDef(variableId)
        );
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitVariableTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.VARIABLE_TYPE;
    }
}
