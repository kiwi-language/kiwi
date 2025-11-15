package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public abstract class Func extends ElementBase implements FuncRef, Executable, Element, ClassScope, GenericDecl, ConstPoolOwner {

    private Name name;
    private List<TypeVar> typeParams = List.nil();
    private List<Param> params = List.nil();
    private Type retType = PrimitiveType.VOID;
    private List<Lambda> lambdas = List.nil();
    private List<Clazz> classes = List.nil();
    protected final ConstPool constPool = new ConstPool();
    private final Code code = new Code(this);

    public Func(Name name) {
        this.name = name;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public List<TypeVar> getTypeParams() {
        return typeParams;
    }

    @Override
    public void addTypeParam(TypeVar typeVar) {
        typeParams = typeParams.append(typeVar);
    }

    @Override
    public List<Param> getParams() {
        return params;
    }

    public List<Type> getParamTypes() {
        return params.map(Param::getType);
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    @Override
    public Type getRetType() {
        return retType;
    }

    @Override
    public void addParam(Param param) {
        params = params.append(param);
    }

    public FuncType getType() {
        return Types.instance.getFuncType(getParamTypes(), retType);
    }

    public List<Lambda> getLambdas() {
        return lambdas;
    }

    public void addLambda(Lambda lambda) {
        lambdas = lambdas.append(lambda);
    }

    @Override
    public List<Clazz> getClasses() {
        return classes;
    }

    @Override
    public void addClass(Clazz clazz) {
        classes = classes.append(clazz);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        typeParams.forEach(action);
        params.forEach(action);
        lambdas.forEach(action);
        classes.forEach(action);
    }


    @Override
    public void write(ElementWriter writer) {
        if (!typeParams.isEmpty()) {
            writer.write("<");
            writer.write(typeParams);
            writer.write("> ");
        }
        writer.write(name);
        writer.write("(");
        writer.write(params);
        writer.write(") -> ");
        writer.writeType(retType);
    }

    public ConstPool getConstPool() {
        return constPool;
    }

    public Code getCode() {
        return code;
    }

    public int getFlags() {
        return 0;
    }

    @Override
    public List<Type> getTypeArgs() {
        return List.into(typeParams);
    }
}
