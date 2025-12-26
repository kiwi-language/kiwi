package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

record Clazz(
        Symbol.ClassSymbol symbol,
        JCTree.JCClassDecl tree,
        Clazz superClass,
        List<Field> instanceFields,
        List<Field> transientFields,
        Field parentField,
        Map<Symbol.TypeSymbol, Adapter> adapters,
        @Nullable Symbol.MethodSymbol onReadMethod,
        List<SubType> subTypes
        ) {

    int pos() {
        return tree.pos;
    }

    int numInstFields() {
        return instanceFields.size();
    }

    Type.ClassType type() {
        return (Type.ClassType) symbol.asType();
    }

    int getNumAdapters() {
        return (superClass != null ? superClass.getNumAdapters() : 0) + adapters.size();
    }

    public @Nullable Adapter findAdapter(Symbol.TypeSymbol sym) {
        var adapter = adapters.get(sym);
        if (adapter != null)
            return adapter;
        if (superClass != null)
            return superClass.findAdapter(sym);
        return null;
    }

    Adapter getAdapter(Symbol.TypeSymbol sym) {
        return Objects.requireNonNull(findAdapter(sym), () -> "Adapter not found for " + sym);
    }

    @Nullable Field getParentField() {
        if (parentField != null)
            return parentField;
        if (superClass != null)
            return superClass.getParentField();
        return null;
    }

    void forEachClazz(Consumer<? super Clazz> action) {
        if (superClass != null)
            superClass.forEachClazz(action);
        action.accept(this);
    }

    void forEachAdapter(Consumer<? super Adapter> action) {
        if (subTypes.nonEmpty()) {
            for (Adapter adapter : adapters.values()) {
                if (adapter.forSubType)
                    action.accept(adapter);
            }
        }
        forEachFieldAdapter(action);
    }

    void forEachFieldAdapter(Consumer<? super Adapter> action) {
        if (superClass != null)
            superClass.forEachFieldAdapter(action);
        for (Adapter adapter : adapters.values()) {
            if (adapter.forField)
                action.accept(adapter);
        }
    }

    int getSubTypeTag() {
        var s = superClass;
        while (s != null) {
            for (SubType subType : s.subTypes) {
                if (subType.symbol() == symbol)
                    return subType.tag();
            }
            s = s.superClass;
        }
        throw new IllegalStateException("Subtype tag not found for " + symbol);
    }

}

record Param(Type type, boolean nullable, boolean padding) {}

record Field(Symbol.VarSymbol symbol, Name name, Type type, Type valueType, boolean nullable, boolean parent, Name adapterName, JCTree.JCExpression init) {}

final class Adapter {
    private final Name name;
    private final Type.ClassType type;
    private final Type.ClassType valueType;
    boolean forField;
    boolean forSubType;

    Adapter(Name name, Type.ClassType type, Type.ClassType valueType) {
        this.name = name;
        this.type = type;
        this.valueType = valueType;
    }

    public Name name() {
        return name;
    }

    public Type.ClassType type() {
        return type;
    }

    public Type.ClassType valueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return "Adapter[" +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "valueType=" + valueType + ']';
    }
}

record SubType(int tag, Symbol.ClassSymbol symbol) {}
