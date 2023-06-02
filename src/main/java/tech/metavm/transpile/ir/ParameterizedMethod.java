package tech.metavm.transpile.ir;

import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public record ParameterizedMethod(
        IRType ownerType,
        IRMethod method,
        List<IRType> typeArguments
) implements GenericDefinition {

    public String toString() {
        return ownerType.toString() +
                ".<" + NncUtils.join(typeArguments, IRType::getName, ",") + ">."
                + method.signature();
    }

    public IRType returnType() {
        return IRUtil.resolveType(method.returnType(), this);
    }

    public List<IRType> parameterTypes() {
        return NncUtils.map(
                method.parameterTypes(),
                t -> IRUtil.resolveType(t, this)
        );
    }

    @Override
    public IRType resolve(TypeVariable typeVariable) {
        int idx = method.typeParameters().indexOf(typeVariable);
        if(idx > 0) {
            return typeArguments.get(idx);
        }
        if(ownerType instanceof PType pType) {
            return pType.resolve(typeVariable);
        }
        throw new InternalException("Can not resolve type variable " + typeVariable);
    }
}
