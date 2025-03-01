package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.function.Consumer;

public class Lambda implements Executable, ValueElement, Constant {

    private final SymName name;
    private List<Parameter> parameters = List.nil();
    private Type returnType = PrimitiveType.NEVER;
    private final Executable enclosing;
    private final Func function;
    private final Code code;

    public Lambda(SymName name, Executable enclosing, Func function) {
        this.name = name;
        this.enclosing = enclosing;
        this.function = function;
        code = new Code(this);
        function.addLambda(this);
    }

    public Executable getEnclosing() {
        return enclosing;
    }

    public Func getFunction() {
        return function;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public void addParameter(Parameter parameter) {
        parameters = parameters.append(parameter);
    }

    @Override
    public SymName getQualifiedName() {
        return function.getQualifiedName().concat(".<lambda>");
    }

    @Override
    public ConstantPool getConstantPool() {
        return function.getConstantPool();
    }

    @Override
    public SymName getName() {
        return name;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambda(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        parameters.forEach(action);
    }

    @Override
    public void write(ElementWriter writer) {

    }

    public @NotNull Code getCode() {
        return code;
    }

    @Override
    public FunctionType getType() {
        return Types.instance.getFunctionType(getParameters().map(LocalVariable::getType), returnType);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.LAMBDA_REF);
        function.getInstance().write(output);
        Elements.writeReference(this, output);
    }
}
