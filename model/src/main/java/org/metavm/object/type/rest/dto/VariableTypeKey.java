package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.jsonk.Json;
import org.metavm.object.instance.core.Id;
import org.metavm.util.Constants;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

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
