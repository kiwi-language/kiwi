package tech.metavm.object.type.rest.dto;

import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import java.util.List;

public record FunctionTypeKey(List<TypeKey> parameterTypeKeys, TypeKey returnTypeKey) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.FUNCTION);
        output.writeInt(parameterTypeKeys.size());
        parameterTypeKeys.forEach(t -> t.write(output));
        returnTypeKey.write(output);
    }

    @Override
    public String toTypeExpression() {
        return "(" + NncUtils.join(parameterTypeKeys, TypeKey::toTypeExpression) + ")->" + returnTypeKey.toTypeExpression();
    }

    @Override
    public FunctionType toType(TypeDefProvider typeDefProvider) {
        return new FunctionType(null, NncUtils.map(parameterTypeKeys, k -> k.toType(typeDefProvider)), returnTypeKey.toType(typeDefProvider));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitFunctionTypeKey(this);
    }

    @Override
    public void acceptChildren(TypeKeyVisitor<?> visitor) {
        parameterTypeKeys.forEach(k -> k.accept(visitor));
        returnTypeKey.accept(visitor);
    }
}
