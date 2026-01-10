package org.manul.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.jsonk.Json;
import org.manul.object.instance.core.Id;
import org.manul.util.Constants;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

@Json
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
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitVariableTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.VARIABLE_TYPE;
    }
}
