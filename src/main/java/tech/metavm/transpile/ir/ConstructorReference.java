package tech.metavm.transpile.ir;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ConstructorReference(
        IRType declaringType,
        IRConstructor constructor,
        List<IRType> typeArguments
) implements IRExpression, IRFunction, HierarchicalGenericDefinition {

    @Override
    public IRType type() {
        return null;
    }

    @Override
    public FunctionType functionType() {
        return new FunctionType(
                List.of(),
                declaringType
        );
    }

    public List<TypeVariable<IRConstructor>> typeParameters() {
        return constructor.typeParameters();
    }

    @Nullable
    @Override
    public GenericDefinition parent() {
        return declaringType instanceof GenericDefinition c ? c : null;
    }

    @Override
    public IRType tryResolve(TypeVariable<?> typeVariable) {
        return constructor.tryResolveType(typeVariable, typeArguments);
    }
}
