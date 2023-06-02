package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.List;

public interface IBlock {

    IBlock parent();

    IValueSymbol tryResolveValue(String name);

    default IValueSymbol resolveValue(String name) {
        return NncUtils.requireNonNull(
                tryResolveValue(name), "Can not resolve symbol '" + name + "'"
        );
    }

    IRClass tryResolveClass(String name);

    ISymbol tryResolveValueOrClass(String name);

    default ISymbol resolveValueOrClass(String name) {
        return NncUtils.requireNonNull(tryResolveValueOrClass(name), "Can not resolve symbol '" + name + "'");
    }

    default IRClass resolveClass(String name) {
        return NncUtils.requireNonNull(
                tryResolveClass(name), "Can not resolve class '" + name + "'"
        );
    }

    default IRMethod tryResolveMethod(String name, List<IRType> parameterTypes) {
        if(parent() != null) {
            return parent().tryResolveMethod(name, parameterTypes);
        }
        else {
            return null;
        }
    }

    default IRMethod resolveMethod(String name, List<IRType> parameterTypes) {
        return NncUtils.requireNonNull(
                tryResolveMethod(name, parameterTypes), "Can not resolve method '" + name + "'"
        );
    }

}