package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class MyTypes {

    private Types types;
    final Type.ClassType object;
    final Type.ClassType string;
    final Type.ClassType date;
    final Type.ClassType record;
    final Type.ClassType clazz;
    final Type.ClassType list;
    final Type.ClassType wireInput;
    final Type.ClassType wireOutput;
    final Type.ClassType wireVisitor;
    final Type.ClassType wireAdapter;
    final Type.ClassType adapterRegistry;
    final Type.JCPrimitiveType initType;
    final Type.JCPrimitiveType byteType;
    final Type.JCPrimitiveType shortType;

    MyTypes(Elements elements, Types types, Symtab symtab) {
        this.types = types;
        var resolver = new TypeResolver(elements, types);
        object = resolver.forName("java.lang.Object");
        string = resolver.forName("java.lang.String");
        date = resolver.forName("java.util.Date");
        record = resolver.forName("java.lang.Record");
        clazz  = resolver.forName("java.lang.Class");
        list = resolver.forName("java.util.List");
        wireInput = resolver.forName("org.metavm.wire.WireInput");
        wireOutput = resolver.forName("org.metavm.wire.WireOutput");
        wireVisitor = resolver.forName("org.metavm.wire.WireVisitor");
        wireAdapter = resolver.forName("org.metavm.wire.WireAdapter");
        adapterRegistry = resolver.forName("org.metavm.wire.AdapterRegistry");
        initType = symtab.intType;
        byteType = symtab.byteType;
        shortType = symtab.shortType;
    }

    private record TypeResolver(Elements elements, Types types) {

        Type.ClassType forName(String name) {
            var cl = elements.getTypeElement(name);
            return (Type.ClassType) types.getDeclaredType(cl);
        }

    }

    Type.ClassType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
        return (Type.ClassType) types.getDeclaredType(typeElem, typeArgs);
    }
    boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return types.isAssignable(t1, t2);
    }

}
