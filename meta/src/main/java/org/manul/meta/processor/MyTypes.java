package org.manul.meta.processor;

import com.sun.tools.javac.code.Type;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MyTypes {

    final TypeMirror value;
    final TypeMirror entity;
    final TypeMirror valueObject;
    final Type classType;
    final Type klass;
    final Type stdKlassRegistry;
    private final Elements elements;
    private final Types types;


    public MyTypes(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
        value = getDeclaredType("org.manul.object.instance.core.Value");
        entity = getDeclaredType("org.manul.entity.Entity");
        valueObject = getDeclaredType("org.manul.api.ValueObject");
        classType = getDeclaredType("org.manul.object.type.ClassType");
        klass = getDeclaredType("org.manul.object.type.Klass");
        stdKlassRegistry = getDeclaredType("org.manul.entity.StdKlassRegistry");
    }

    public Type getDeclaredType(String fqn, TypeMirror...typeArgs) {
        var cl = elements.getTypeElement(fqn);
        return cl != null ? (Type) types.getDeclaredType(cl, typeArgs) : (Type) types.getNoType(TypeKind.NONE);
    }

    public boolean isAssignable(TypeMirror from, TypeMirror to) {
        if (to.getKind() == TypeKind.NONE)
            return false;
        return types.isAssignable(from, to);
    }

}
