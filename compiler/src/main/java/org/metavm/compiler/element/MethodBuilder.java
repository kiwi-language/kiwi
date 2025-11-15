package org.metavm.compiler.element;

import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public class MethodBuilder {

    public static MethodBuilder newBuilder(Clazz clazz, Name name) {
        return new MethodBuilder(clazz, name);
    }

    private final Clazz clazz;
    private final Name name;
    private Access access = Access.PUBLIC;
    private boolean isStatic = false;
    private boolean isAbstract = false;
    private boolean isInit = false;
    private Type retType = PrimitiveType.VOID;
    private final List.Builder<ParamInfo> params = List.builder();

    private MethodBuilder(Clazz clazz, Name name) {
        this.clazz = clazz;
        this.name = name;
    }

    public MethodBuilder addParam(Name name, Type type) {
        params.append(new ParamInfo(name, type));
        return this;
    }

    public MethodBuilder retType(Type retType) {
        this.retType = retType;
        return this;
    }

    public MethodBuilder access(Access access) {
        this.access = access;
        return this;
    }

    public MethodBuilder isStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return this;
    }

    public MethodBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public MethodBuilder isInit(boolean isInit) {
        this.isInit = isInit;
        return this;
    }

    private record ParamInfo(Name name, Type type) {}

    public Method build() {
        var method = new Method(name, access, isStatic, isAbstract, isInit, clazz);
        params.forEach(p -> new Param(p.name, p.type, method));
        method.setRetType(retType);
        return method;
    }


}
