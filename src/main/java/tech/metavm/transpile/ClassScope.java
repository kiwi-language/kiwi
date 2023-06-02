package tech.metavm.transpile;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassScope extends ScopeBase {

    @Nullable
    private final String name;
    @Nullable
    private final ClassScope superClass;
    private final boolean isStatic;
    private final IRType type;
    private final Map<SymbolIdentifier, Symbol> instanceMemberMap = new HashMap<>();
    private final Map<SymbolIdentifier, Symbol> staticMemberMap = new HashMap<>();
    private final Map<String, Integer> overloadCountMap = new HashMap<>();
//    private final Map<MethodSignature, String> signature2methodName = new HashMap<>();

    public ClassScope(Scope parent, IRType type, @Nullable String name, boolean isStatic, @Nullable ClassScope superClass) {
        super(parent);
        this.type = type;
        this.name = name;
        this.isStatic = isStatic;
        this.superClass = superClass;
    }

    public SourceType getSourceType() {
        return SourceTypeUtil.fromClassName(name);
    }

    private Symbol resolveParent(SymbolIdentifier identifier, boolean excludeInstance) {
        if(parent instanceof ClassScope classScope) {
            return classScope.resolve(identifier, excludeInstance);
        }
        return parent != null ? parent.resolve(identifier) : null;
    }

    public IRType getType() {
        return type;
    }

    public Symbol tryResolve(SymbolIdentifier identifier, boolean excludeInstance) {
        if(!excludeInstance) {
            var symbol = instanceMemberMap.get(identifier);
            if (symbol != null) {
                return symbol;
            }
        }
        var symbol = staticMemberMap.get(identifier);
        if(symbol != null) {
            return symbol;
        }
        if(parent instanceof ClassScope enclosingClass) {
            symbol = enclosingClass.tryResolve(identifier, excludeInstance || isStatic);
            if(symbol != null) {
                return symbol;
            }
        }
        if(superClass != null) {
            symbol = superClass.tryResolve(identifier, excludeInstance || isStatic);
            if(symbol != null) {
                return symbol;
            }
        }
        return parent != null ? parent.tryResolve(identifier) : null;
    }

    public Symbol resolve(SymbolIdentifier identifier, boolean excludeInstance) {
        var resolved = tryResolve(identifier, excludeInstance);
        if(resolved != null) {
            return resolved;
        }
        else {
            throw new InternalException("Can not resolve symbol '" + identifier + "' in class '" + name + "'");
        }
    }

    public void defineField(String name, Type type, boolean isStatic) {
        defineMember(SymbolIdentifier.variable(name), name, new ReflectSourceType(type), isStatic);
    }

    public void defineMethod(String name, List<SourceType> parameterTypeNames, SourceType type, boolean isStatic) {
        int count = overloadCountMap.compute(name, (k, v) -> v == null ? 0 : v + 1);
        var tsName = count == 0 ? name : name + count;
        defineMember(SymbolIdentifier.method(name, parameterTypeNames), tsName, type, isStatic);
    }

    public void defineType(String name, boolean isStatic) {
        defineMember(SymbolIdentifier.type(name), name, null, isStatic);
    }

    private void defineMember(SymbolIdentifier identifier, String name, SourceType type, boolean isStatic) {
        var map = isStatic ? staticMemberMap : instanceMemberMap;
        var prefix = isStatic ? getSimpleName() : "this";
        map.put(
                identifier,
                new Symbol(identifier, prefix, name, type)
        );
    }

    @Nullable
    public String getName() {
        return name;
    }

    private String getSimpleName() {
        if(name == null) {
            return null;
        }
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Nullable
    public ClassScope getSuperClass() {
        return superClass;
    }

    public ClassScope getSuperClassRequired() {
        return NncUtils.requireNonNull(
                getSuperClass(), "Class '" + name + "' doesn't have a super class"
        );
    }

    @Override
    protected Symbol resolveSelf(SymbolIdentifier identifier) {
        return tryResolve(identifier, false);
    }
}
