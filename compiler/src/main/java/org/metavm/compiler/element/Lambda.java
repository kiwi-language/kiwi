package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.generate.KlassOutput;
import org.metavm.compiler.generate.WireTypes;

import java.util.function.Consumer;

public class Lambda extends ElementBase implements Executable, ValueElement, Constant {

    private final Name name;
    private List<Param> params = List.nil();
    private Type retType = PrimitiveType.NEVER;
    private Func function;
    private Code code;

    public Lambda(Name name) {
        this.name = name;
    }

    public Func getFunction() {
        return function;
    }

    public void attachTo(Func function) {
        assert this.function == null : "Lambda is already attached";
        this.function = function;
        function.addLambda(this);
        code = new Code(this);
    }

    @Override
    public List<Param> getParams() {
        return params;
    }

    @Override
    public Type getRetType() {
        return retType;
    }

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    @Override
    public void addParam(Param param) {
        params = params.append(param);
    }

    @Override
    public Name getQualName() {
//        return function.getQualifiedName().concat(".<lambda>");
        return Name.from("<lambda>");
    }

    @Override
    public ConstPool getConstPool() {
        return function.getConstPool();
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambda(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        params.forEach(action);
    }

    @Override
    public void write(ElementWriter writer) {

    }

    public @NotNull Code getCode() {
        return code;
    }

    @Override
    public FuncType getType() {
        return Types.instance.getFuncType(getParams().map(LocalVar::getType), retType);
    }

    @Override
    public void write(KlassOutput output) {
        output.write(WireTypes.LAMBDA_REF);
        function.write(output);
        Elements.writeReference(this, output);
    }
}
