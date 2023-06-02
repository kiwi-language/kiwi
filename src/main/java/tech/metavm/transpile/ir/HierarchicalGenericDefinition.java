package tech.metavm.transpile.ir;

import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

public interface HierarchicalGenericDefinition extends GenericDefinition {

    @Override
    default IRType resolve(TypeVariable<?> typeVariable) {
        var resolved = tryResolve(typeVariable);
        if(resolved != null) {
            return resolved;
        }
        var p = parent();
        if(p != null) {
            return p.resolve(typeVariable);
        }
        throw new InternalException("Can not resole type variable: " + typeVariable);
    }

    @Nullable
    GenericDefinition parent();

    IRType tryResolve(TypeVariable<?> typeVariable);

}
