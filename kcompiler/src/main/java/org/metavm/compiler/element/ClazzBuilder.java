package org.metavm.compiler.element;

import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public class ClazzBuilder {

    public static ClazzBuilder newBuilder(Name name, ClassScope scope) {
        return new ClazzBuilder(name, scope);
    }

    private final Name name;
    private ClassTag tag = ClassTag.CLASS;
    private Access access = Access.PUBLIC;
    private List<ClassType> interfaces = List.nil();
    private List<TypeParamInfo> typeParams = List.nil();
    private final ClassScope scope;

    public ClazzBuilder(Name name, ClassScope scope) {
        this.name = name;
        this.scope = scope;
    }

    public ClazzBuilder tag(ClassTag tag) {
        this.tag = tag;
        return this;
    }

    public ClazzBuilder access(Access access) {
        this.access = access;
        return this;
    }

    public ClazzBuilder interfaces(List<ClassType> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public ClazzBuilder addTypeParam(Name name) {
        return addTypeParam(name, PrimitiveType.ANY);
    }

    public ClazzBuilder addTypeParam(Name name, Type bound) {
        typeParams = typeParams.append(new TypeParamInfo(name, bound));
        return this;
    }

    private record TypeParamInfo(Name name, Type bound) {}

    public Clazz build() {
        var clazz =  new Clazz(tag, name, access, scope);
        clazz.setInterfaces(interfaces);
        for (TypeParamInfo typeParam : typeParams) {
            new TypeVar(typeParam.name, typeParam.bound, clazz);
        }
        return clazz;
    }

}
