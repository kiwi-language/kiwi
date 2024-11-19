package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.CapturedType;
import org.metavm.object.type.CapturedTypeVariable;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record CapturedTypeKey(@NotNull Id variableId) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.writeId(variableId);
    }

    @Override
    public String toTypeExpression() {
        return String.format("#$$%s", variableId);
    }

    @Override
    public CapturedType toType(TypeDefProvider typeDefProvider) {
        return new CapturedType((CapturedTypeVariable) typeDefProvider.getTypeDef(variableId));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitCapturedTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.CAPTURED_TYPE;
    }
}
