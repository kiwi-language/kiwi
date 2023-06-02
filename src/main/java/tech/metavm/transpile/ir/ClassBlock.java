package tech.metavm.transpile.ir;

import java.util.List;

public record ClassBlock(
        IBlock parent,
        IRClass klass) implements IBlock {

    @Override
    public IValueSymbol tryResolveValue(String name) {
        var f = klass.tryGetField(name);
        return f != null ? f: parent.tryResolveValue(name);
    }

    @Override
    public IRClass tryResolveClass(String name) {
        var k = klass.tryGetClass(name);
        return k != null ? k: parent.tryResolveClass(name);
    }

    @Override
    public ISymbol tryResolveValueOrClass(String name) {
        var field = klass.tryGetField(name);
        if(field != null) {
            return field;
        }
        var k = klass.tryGetClass(name);
        return k != null ? k : parent.tryResolveValueOrClass(name);
    }

    @Override
    public IRMethod tryResolveMethod(String name, List<IRType> parameterTypes) {
        var method = klass.tryGetMethod(name, parameterTypes);
        return method != null ? method : parent.tryResolveMethod(name, parameterTypes);
    }

}
