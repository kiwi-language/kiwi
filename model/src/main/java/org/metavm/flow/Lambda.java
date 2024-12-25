package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ChildArray;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.*;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class Lambda extends Element implements Callable, ITypeDef {

    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private int returnTypeIndex;
    private int typeIndex;
    @ChildEntity
    private final Code code;
    private Flow flow;

    public Lambda(Long tmpId, List<Parameter> parameters, @NotNull Type returnType, Flow flow) {
        super(tmpId);
        this.returnTypeIndex = flow.getConstantPool().addValue(returnType);
        setParameters(parameters, false);
        this.typeIndex = flow.getConstantPool().addValue(Types.getFunctionType(parameters, returnType));
        this.code = addChild(new Code(this), "code");
        this.flow = flow;
        flow.addLambda(this);
    }

    @Override
    public Type getReturnType() {
        return flow.getConstantPool().getType(returnTypeIndex);
    }

    public void setReturnType(Type returnType) {
        this.returnTypeIndex = flow.getConstantPool().addValue(returnType);
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
        typeIndex = flow.getConstantPool().addValue(new FunctionType(getParameterTypes(), getReturnType()));
//        setOutputType(functionalInterface != null ? functionalInterface : functionType);
    }

    @Override
    public int getTypeIndex() {
        return typeIndex;
    }

    @Override
    public FunctionType getFunctionType() {
        return flow.getConstantPool().getFunctionType(typeIndex);
    }

    @Override
    public LambdaRef getRef() {
        return new LambdaRef(getFlow().getRef(), this);
    }

    public Code getCode() {
        return code;
    }

    @Override
    public ConstantPool getConstantPool() {
        return flow.getConstantPool();
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

    public void write(MvOutput output) {
        output.writeEntityId(this);
        int paramCount = parameters.size();
        output.writeInt(paramCount);
        for (Parameter parameter : parameters) {
            parameter.write(output);
        }
        output.writeShort(returnTypeIndex);
        output.writeShort(typeIndex);
        code.write(output);
    }

    public void read(KlassInput input) {
        int paramCount = input.readInt();
        var params = new ArrayList<Parameter>();
        for (int i = 0; i < paramCount; i++) {
            params.add(input.readParameter());
        }
        setParameters(params);
        returnTypeIndex = input.readShort();
        typeIndex = input.readShort();
        code.read(input);
    }

    public Flow getFlow() {
        return flow;
    }

    void setFlow(Flow flow) {
        this.flow = flow;
    }
}
