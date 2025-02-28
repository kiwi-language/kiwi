package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(66)
public class Lambda extends Entity implements Callable, ITypeDef, Element {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private String name;
    private List<Parameter> parameters = new ArrayList<>();
    private int returnTypeIndex;
    private Code code;
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

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitList(visitor::visitEntity);
        visitor.visitInt();
        Code.visit(visitor);
    }

    @Override
    public Type getReturnType() {
        return flow.getConstantPool().getType(returnTypeIndex);
    }

    public void setReturnType(Type returnType) {
        this.returnTypeIndex = flow.getConstantPool().addValue(returnType);
    }

    public void setReturnTypeIndex(int returnTypeIndex) {
        this.returnTypeIndex = returnTypeIndex;
    }

    public int getReturnTypeIndex() {
        return returnTypeIndex;
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

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
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

    public String getName() {
        return name;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var parameters_ : parameters) action.accept(parameters_.getReference());
        code.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("returnType", this.getReturnType().toJson());
        map.put("returnTypeIndex", this.getReturnTypeIndex());
        map.put("parameters", this.getParameters().stream().map(Entity::getStringId).toList());
        map.put("functionType", this.getFunctionType().toJson());
        map.put("ref", this.getRef().toJson());
        map.put("code", this.getCode().toJson());
        map.put("constantPool", this.getConstantPool().toJson());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        map.put("parameterTypes", this.getParameterTypes().stream().map(Type::toJson).toList());
        map.put("minLocals", this.getMinLocals());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (var parameters_ : parameters) action.accept(parameters_);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Lambda;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        this.flow = (Flow) parent;
        this.name = input.readUTF();
        this.parameters = input.readList(() -> input.readEntity(Parameter.class, this));
        this.returnTypeIndex = input.readInt();
        this.code = Code.read(input, this);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeList(parameters, output::writeEntity);
        output.writeInt(returnTypeIndex);
        code.write(output);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
