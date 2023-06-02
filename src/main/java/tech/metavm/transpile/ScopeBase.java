package tech.metavm.transpile;

import tech.metavm.util.InternalException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ScopeBase implements Scope {

    protected final Scope parent;
    private final Map<SymbolIdentifier, Symbol> resolvedVariables = new HashMap<>();

    public ScopeBase(Scope parent) {
        this.parent = parent;
    }

    @Override
    public Symbol resolve(Set<SymbolKind> kinds, String name) {
        var v = tryResolve(kinds, name);
        if(v != null) {
            return v;
        }
        throw new InternalException("Can not resolve variable '" + name + "'");
    }

    @Override
    public Symbol tryResolve(Set<SymbolKind> kinds, String name) {
        for (SymbolKind kind : kinds) {
            var identifier = new SymbolIdentifier(kind, name, null);
            Symbol selfVar = lookupSelfWithCache(identifier);
            if (selfVar != null) {
                return selfVar;
            }
        }
        return parent != null ? parent.tryResolve(kinds, name) : null;
    }

    private Symbol lookupSelfWithCache(SymbolIdentifier identifier) {
        var resolved = resolvedVariables.get(identifier);
        if(resolved != null) {
            return resolved;
        }
        resolved = resolveSelf(identifier);
        if(resolved != null) {
            resolvedVariables.put(resolved.identifier(), resolved);
            return resolved;
        }
        return null;
    }

    protected abstract Symbol resolveSelf(SymbolIdentifier identifier);

}
