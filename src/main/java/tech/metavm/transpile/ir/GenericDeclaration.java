package tech.metavm.transpile.ir;

import java.util.List;

public interface GenericDeclaration<T extends GenericDeclaration<T>> {

    List<TypeVariable<T>> typeParameters();

    default boolean isTypeArgumentsMatched(List<IRType> typeArguments) {
        if(typeParameters().size() != typeArguments.size()) {
            return false;
        }
        for (int i = 0; i < typeParameters().size(); i++) {
            var param = typeParameters().get(i);
            var arg = typeArguments.get(i);
            if(!param.isWithinRange(arg)) {
                return false;
            }
        }
        return true;
    }

    default IRType tryResolveType(TypeVariable<?> typeVariable, List<IRType> typeArgument) {
        int idx = typeParameters().indexOf(typeVariable);
        return idx >= 0 ? typeArgument.get(idx) : null;
    }

    default List<GenericDefinition> getDeclaredContexts() {
        return List.of();
    }

}
