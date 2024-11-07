package org.metavm.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.flow.Callable;
import org.metavm.flow.Lambda;
import org.metavm.flow.Method;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AsmCallable implements AsmScope {
    final AsmScope parent;
    final Callable callable;
    final Map<String, AsmVariable> variableMap = new HashMap<>();
    int nextVariableIndex;

    AsmCallable(AsmScope parent, Callable callable) {
        this.parent = parent;
        this.callable = callable;
    }

    AsmVariable declareVariable(String name, Type type) {
        var v = new AsmVariable(name, nextVariableIndex(), type, this);
        variableMap.put(name, v);
        return v;
    }

    int nextVariableIndex() {
        return nextVariableIndex++;
    }

    AsmVariable resolveVariable(String name) {
        var v = tryResolveVariable(name);
        if(v != null)
            return v;
        throw new IllegalStateException("Cannot resolve variable " + name);
    }

    @Nullable AsmVariable tryResolveVariable(String name) {
        var v = variableMap.get(name);
        if (v != null)
            return v;
        if (parent instanceof AsmCallable c)
            return c.tryResolveVariable(name);
        return null;
    }

    public boolean isLambda() {
        return callable instanceof Lambda;
    }

    public int getMethodContextIndex() {
        var cIdx = -1;
        AsmScope s = this;
        while (s instanceof AsmCallable c && c.isLambda()) {
            s = c.parent;
            cIdx++;
        }
        NncUtils.requireTrue(s instanceof AsmMethod);
        return cIdx;
    }

    public Klass getDeclaringKlass() {
        return ((Method) callable.getScope().getFlow()).getDeclaringType();
    }

    public Callable getCallable() {
        return callable;
    }

    @NotNull
    @Override
    public AsmScope parent() {
        return parent;
    }

    @Override
    public AsmCompilationUnit getCompilationUnit() {
        return parent.getCompilationUnit();
    }

}
