package tech.metavm.transpile;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface Scope {

    default Symbol resolve(SymbolKind kind, String name) {
        return resolve(EnumSet.of(kind), name);
    }

    Symbol resolve(Set<SymbolKind> kinds, String name);

    default Symbol resolve(SymbolIdentifier identifier) {
        return resolve(identifier.kind(), identifier.name());
    }

    default Symbol resolveMethod(String name, List<SourceType> parameterTypes) {
        return resolve(SymbolIdentifier.method(name, parameterTypes));
    }

    default Symbol resolveVariable(String name) {
        return resolve(SymbolKind.VARIABLE, name);
    }

    default Symbol resolveType(String name) {
        return resolve(SymbolKind.TYPE, name);
    }

    default Symbol resolve(String name) {
        return resolve(EnumSet.allOf(SymbolKind.class), name);
    }

    Symbol tryResolve(Set<SymbolKind> kinds, String name);


    default Symbol tryResolve(SymbolIdentifier identifier) {
        return tryResolve(EnumSet.of(identifier.kind()), identifier.name());
    }

    default Symbol tryResolve(String name) {
        return tryResolve(null, name);
    }

    default Symbol tryResolveVariable(String name) {
        return tryResolve(EnumSet.of(SymbolKind.VARIABLE), name);
    }

    default Symbol tryResolveMethod(String name) {
        return tryResolve(EnumSet.of(SymbolKind.METHOD), name);
    }

    default Symbol tryResolveType(String name) {
        return tryResolve(EnumSet.of(SymbolKind.TYPE), name);
    }

}
