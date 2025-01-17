package org.metavm.object.type.rest.dto;

import org.metavm.object.type.FunctionType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import java.util.List;

public record FunctionTypeKey(List<TypeKey> parameterTypeKeys, TypeKey returnTypeKey) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_TYPE);
        output.writeInt(parameterTypeKeys.size());
        parameterTypeKeys.forEach(t -> t.write(output));
        returnTypeKey.write(output);
    }

    @Override
    public String toTypeExpression() {
        return "(" + Utils.join(parameterTypeKeys, TypeKey::toTypeExpression) + ")->" + returnTypeKey.toTypeExpression();
    }

    @Override
    public FunctionType toType(TypeDefProvider typeDefProvider) {
        return new FunctionType(Utils.map(parameterTypeKeys, k -> k.toType(typeDefProvider)), returnTypeKey.toType(typeDefProvider));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitFunctionTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.FUNCTION_TYPE;
    }
}
