package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ChildArray;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class Lambda extends Element implements Callable, ITypeDef {

    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private Type returnType;
    private FunctionType functionType;
    @ChildEntity
    private final Code code;
    private Flow flow;

    public Lambda(Long tmpId, List<Parameter> parameters, @NotNull Type returnType, Flow flow) {
        super(tmpId);
        this.returnType = returnType;
        setParameters(parameters, false);
        this.functionType = Types.getFunctionType(parameters, returnType);
        this.code = addChild(new Code(this), "code");
        this.flow = flow;
        flow.addLambda(this);
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
        resetType();
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters.toList();
    }

    @Override
    public int getInputCount() {
        return parameters.size();
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean resetType) {
        NncUtils.forEach(parameters, p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
        if (resetType)
            resetType();
    }

    @Override
    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    private void resetType() {
        functionType = new FunctionType(getParameterTypes(), returnType);
//        setOutputType(functionalInterface != null ? functionalInterface : functionType);
    }

    @Override
    public FunctionType getFunctionType() {
        return functionType;
    }

    @Override
    public LambdaRef getRef() {
        return new LambdaRef(this);
    }

    public Code getCode() {
        return code;
    }

    public void emitCode() {
        code.emitCode();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambda(this);
    }

    public String getText() {
        CodeWriter writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    public void writeCode(CodeWriter writer) {
        writer.writeNewLine(
                "Lambda "
                        + " (" + NncUtils.join(parameters, Parameter::getText, ", ")
                        + ")"
                        + ": " + getReturnType().getName()
        );
        getCode().writeCode(writer);
    }

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        int paramCount = parameters.size();
        output.writeInt(paramCount);
        for (Parameter parameter : parameters) {
            parameter.write(output);
        }
        returnType.write(output);
        functionType.write(output);
        code.write(output);
    }

    public void read(KlassInput input) {
        int paramCount = input.readInt();
        var params = new ArrayList<Parameter>();
        for (int i = 0; i < paramCount; i++) {
            params.add(input.readParameter());
        }
        setParameters(params);
        returnType = input.readType();
        functionType = (FunctionType) input.readType();
        code.read(input);
    }

    public Flow getFlow() {
        return flow;
    }

    void setFlow(Flow flow) {
        this.flow = flow;
    }
}
