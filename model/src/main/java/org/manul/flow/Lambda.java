package org.manul.flow;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.manul.api.JsonIgnore;
import org.manul.entity.*;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.*;
import org.manul.util.Utils;
import org.manul.wire.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@org.manul.api.Entity
@Wire(66)
public class Lambda extends Entity implements Callable, ITypeDef, Element {

    @Getter
    private final String name;
    private final List<Parameter> parameters = new ArrayList<>();
    @Getter
    @Setter
    private int returnTypeIndex;
    @Getter
    private final Code code;
    @Setter
    @Getter
    @Parent
    private Flow flow;

    public Lambda(@NotNull Id id, String name, List<Parameter> parameters, @NotNull Type returnType, Flow flow) {
        this(id, name, parameters, flow.getConstantPool().addValue(returnType), flow);
    }

    public Lambda(@NotNull Id id, String name, List<Parameter> parameters, int returnTypeIndex, Flow flow) {
        super(id);
        this.name = name;
        this.returnTypeIndex = returnTypeIndex;
        setParameters(parameters);
        this.code = new Code(this);
        this.flow = flow;
        flow.addLambda(this);
    }

    @Override
    public Type getReturnType() {
        return flow.getConstantPool().getType(returnTypeIndex);
    }

    public void setReturnType(Type returnType) {
        this.returnTypeIndex = flow.getConstantPool().addValue(returnType);
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    @Override
    public int getInputCount() {
        return parameters.size();
    }

    public void setParameters(List<Parameter> parameters) {
        Utils.forEach(parameters, p -> p.setCallable(this));
        this.parameters.clear();
        this.parameters.addAll(parameters);
    }

    @Override
    public Parameter getParameterByName(String name) {
        return Utils.findRequired(parameters, p -> p.getName().equals(name));
    }

    @Override
    public FunctionType getFunctionType() {
        return getType(flow.getConstantPool());
    }

    @Override
    public LambdaRef getRef() {
        return new LambdaRef(getFlow().getRef(), this);
    }

    @Override
    public ConstantPool getConstantPool() {
        return flow.getConstantPool();
    }

    public void emitCode() {
        code.emitCode();
    }

    @JsonIgnore
    public String getText() {
        CodeWriter writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    public void writeCode(CodeWriter writer) {
        writer.writeln(
                "Lambda "
                        + " (" + Utils.join(parameters, Parameter::getText, ", ")
                        + ")"
                        + ": " + getReturnType().getName()
        );
        getCode().writeCode(writer);
    }

    @Override
    public String getTitle() {
        return "<lambda>";
    }

    @Nullable
    @Override
    public Entity getParentEntity() {
        return flow;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambda(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        parameters.forEach(arg -> arg.accept(visitor));
        code.accept(visitor);
    }

    public FunctionType getType(TypeMetadata typeMetadata) {
        return new FunctionType(getParameterTypes(typeMetadata), getReturnType());
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var parameters_ : parameters) action.accept(parameters_.getReference());
        code.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (var parameters_ : parameters) action.accept(parameters_);
    }

}
