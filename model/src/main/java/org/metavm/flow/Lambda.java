package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ChildArray;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LambdaDTO;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import java.util.List;

public class Lambda extends Element implements Callable {

    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private Type returnType;
    private FunctionType functionType;
    @ChildEntity
    private final ScopeRT scope;

    public Lambda(Long tmpId, List<Parameter> parameters, @NotNull Type returnType, Flow flow) {
        super(tmpId);
        this.returnType = returnType;
        setParameters(parameters, false);
        this.functionType = Types.getFunctionType(parameters, returnType);
        this.scope = addChild(new ScopeRT(this, flow), "scope");
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

    public ScopeRT getScope() {
        return scope;
    }

    public void emitCode() {
        scope.emitCode();
    }

    public LambdaDTO toDTO(SerializeContext serializeContext) {
        return new LambdaDTO(
                serializeContext.getStringId(this),
                NncUtils.map(parameters, Parameter::toDTO),
                returnType.toExpression(serializeContext),
                scope.toDTO(serializeContext)
        );
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
        getScope().writeCode(writer);
    }

}
