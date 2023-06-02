package tech.metavm.transpile;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;

import java.util.*;

class RootBlock implements IBlock {

    private final Map<String, IRClass> classMap = new HashMap<>();
    private final Map<String, IRMethod> methodMap = new HashMap<>();
    private final Map<String, IRField> fieldMap = new HashMap<>();
    private final Set<IRPackage> importedPackages = new HashSet<>();

    void addStaticField(IRField field) {
        fieldMap.put(field.name(), field);
    }

    void addStaticMethod(IRMethod method) {
        methodMap.put(method.signature(), method);
    }

    void addClass(IRClass klass) {
        classMap.put(klass.getSimpleName(), klass);
    }

    void addAllStaticMembers(IRClass klass) {
        klass.staticFields().forEach(this::addStaticField);
        klass.staticMethods().forEach(this::addStaticMethod);
        klass.staticClasses().forEach(this::addClass);
    }

    void addPackage(IRPackage pkg) {
        importedPackages.add(pkg);
    }

    String getQualifiedName(String name) {
        if(name.contains(".")) {
            return name;
        }
        var klass = classMap.get(name);
        if(klass != null) {
            return klass.getName();
        }
        for (IRPackage pkg : importedPackages) {
            klass = pkg.tryGetClass(name);
            if(klass != null) {
                return klass.getName();
            }
        }
        throw new InternalException("Can not get qualified name for: " + name);
    }


    @Override
    public IBlock parent() {
        return null;
    }

    @Override
    public IValueSymbol tryResolveValue(String name) {
        return fieldMap.get(name);
    }

    @Override
    public IRClass tryResolveClass(String name) {
        return classMap.get(name);
    }

    @Override
    public ISymbol tryResolveValueOrClass(String name) {
        var value = tryResolveValue(name);
        return value != null ? value : tryResolveClass(name);
    }

    @Override
    public IRMethod tryResolveMethod(String name, List<IRType> parameterTypes) {
        return methodMap.get(IRMethod.buildSignature(name, parameterTypes));
    }
}
