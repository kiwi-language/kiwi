package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.util.List;

import javax.lang.model.util.Elements;

class MyClasses {

    final Symbol.ClassSymbol object;
    final Symbol.ClassSymbol string;
    final Symbol.ClassSymbol record;
    final Symbol.ClassSymbol list;
    final Symbol.ClassSymbol date;
    final Symbol.ClassSymbol nullable;
    final Symbol.ClassSymbol wireInput;
    final Symbol.ClassSymbol wireOutput;
    final Symbol.ClassSymbol wireVisitor;
    final Symbol.ClassSymbol wireAdapter;
    final Symbol.ClassSymbol adapterRegistry;
    final Symbol.ClassSymbol wire;
    final Symbol.ClassSymbol parent;
    final Symbol.ClassSymbol enumCode;
    final Symbol.ClassSymbol enumFromCode;
    final Symbol.ClassSymbol unsupportedOperationException;
    final Symbol.ClassSymbol wireIOException;
    final Symbol.ClassSymbol _byte;
    final Symbol.ClassSymbol _short;
    final Symbol.ClassSymbol integer;
    final Symbol.ClassSymbol _long;
    final Symbol.ClassSymbol _float;
    final Symbol.ClassSymbol _double;
    final Symbol.ClassSymbol _boolean;
    final Symbol.ClassSymbol character;

    public MyClasses(Elements elements) {
        var resolver = new Resolver(elements);
        object = resolver.forName("java.lang.Object");
        string = resolver.forName("java.lang.String");
        record = resolver.forName("java.lang.Record");
        list = resolver.forName("java.util.List");
        date = resolver.forName("java.util.Date");
        nullable = resolver.forName("javax.annotation.Nullable");
        wireInput = resolver.forName("org.metavm.wire.WireInput");
        wireOutput = resolver.forName("org.metavm.wire.WireOutput");
        wireVisitor = resolver.forName("org.metavm.wire.WireVisitor");
        wireAdapter = resolver.forName("org.metavm.wire.WireAdapter");
        adapterRegistry = resolver.forName("org.metavm.wire.AdapterRegistry");
        wire = resolver.forName("org.metavm.wire.Wire");
        parent = resolver.forName("org.metavm.wire.Parent");
        enumCode = resolver.forName("org.metavm.entity.EnumCode");
        enumFromCode = resolver.forName("org.metavm.entity.EnumFromCode");
        unsupportedOperationException = resolver.forName("java.lang.UnsupportedOperationException");
        wireIOException = resolver.forName("org.metavm.wire.WireIOException");
        _byte = resolver.forName("java.lang.Byte");
        _short = resolver.forName("java.lang.Short");
        integer = resolver.forName("java.lang.Integer");
        _long = resolver.forName("java.lang.Long");
        _float = resolver.forName("java.lang.Float");
        _double = resolver.forName("java.lang.Double");
        _boolean = resolver.forName("java.lang.Boolean");
        character = resolver.forName("java.lang.Character");
    }

    private record Resolver(Elements elements) {

        Symbol.ClassSymbol forName(String name) {
            return (Symbol.ClassSymbol) elements.getTypeElement(name);
        }

    }

    public List<Symbol.ClassSymbol> getClasses(Symbol.ClassSymbol clazz) {
        var classes = List.<Symbol.ClassSymbol>nil();
        var s = clazz;
        for (;;) {
            classes = classes.prepend(s);
            var sup = s.getSuperclass();
            if (sup.getTag() != TypeTag.NONE && sup.tsym != object) {
                s = (Symbol.ClassSymbol) sup.tsym;
            } else
                return classes;
        }
    }

    public boolean isPrimitiveWrapper(Symbol.ClassSymbol clazz) {
        return clazz == _byte || clazz == _short || clazz == integer || clazz == _long
                || clazz == _float || clazz == _double || clazz == character || clazz == _boolean;
    }

}
