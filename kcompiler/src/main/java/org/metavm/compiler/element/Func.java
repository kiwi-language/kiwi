package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public abstract class Func implements Executable, Element, ClassScope, GenericDeclaration {

    private SymName name;
    private List<TypeVariable> typeParameters = List.nil();
    private List<Parameter> parameters = List.nil();
    private Type returnType = PrimitiveType.VOID;
    private List<Lambda> lambdas = List.nil();
    private List<Clazz> classes = List.nil();
    protected final ConstantPool constantPool = new ConstantPool();
    private final Code code = new Code(this);

    public Func(SymName name) {
        this.name = name;
    }

    public SymName getName() {
        return name;
    }

    public void setName(SymName name) {
        this.name = name;
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public void addTypeParameter(TypeVariable typeVariable) {
        typeParameters = typeParameters.append(typeVariable);
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Type> getParameterTypes() {
        return parameters.map(Parameter::getType);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public void addParameter(Parameter parameter) {
        parameters = parameters.append(parameter);
    }

    public List<Lambda> getLambdas() {
        return lambdas;
    }

    void addLambda(Lambda lambda) {
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
        typeParameters.forEach(action);
        parameters.forEach(action);
        lambdas.forEach(action);
        classes.forEach(action);
    }


    @Override
    public void write(ElementWriter writer) {
        if (!typeParameters.isEmpty()) {
            writer.write("<");
            writer.write(typeParameters);
            writer.write("> ");
        }
        writer.write(name);
        writer.write("(");
        writer.write(parameters);
        writer.write(") -> ");
        writer.writeType(returnType);
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public Code getCode() {
        return code;
    }

    public int getFlags() {
        return 0;
    }

    public abstract FuncInst getInstance();

}
