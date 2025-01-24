package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.GlobalKey;
import org.metavm.entity.IndexDef;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.entity.natives.FunctionImpl;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(67)
@Entity(searchable = true)
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_ALL_FLAG = IndexDef.create(Function.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));

    public static final IndexDef<Function> UNIQUE_NAME =
            IndexDef.createUnique(Function.class, 1,
                    function -> List.of(Instances.stringInstance(function.getName())));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    private transient @Nullable FunctionImpl nativeCode;

    public Function(Long tmpId,
                    String name,
                    boolean isNative,
                    boolean isSynthetic,
                    List<NameAndType> parameters,
                    int returnTypeIndex,
                    List<TypeVariable> typeParameters,
                    MetadataState state) {
        super(tmpId, name, isNative, isSynthetic, parameters, returnTypeIndex, typeParameters, state);
        resetBody();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Flow.visitBody(visitor);
        visitor.visitBoolean();
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getName();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public String toString() {
        return "Function " + getName();
    }

    @Override
    public FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext) {
        Utils.require(self == null);
        checkArguments(arguments, flowRef.getTypeMetadata());
        if (isNative()) {
            return Objects.requireNonNull(
                    nativeCode, "Native function " + this + " is not initialized"
            ).run((FunctionRef) flowRef, arguments, callContext);
        }
        else
            return VmStack.execute(
                    flowRef,
                    arguments.toArray(Value[]::new),
                    null,
                    new DefaultCallContext(callContext.instanceRepository())
            );
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return getName() + "(" +
                Utils.join(getParameterTypes(), t -> t.getInternalName(this), ",") + ")";
    }

    @Override
    public FunctionRef getRef() {
        return new FunctionRef(this, List.of());
    }

    public void setNativeCode(FunctionImpl impl) {
        Utils.require(isNative(), "Function " + this + " is not native");
        this.nativeCode = impl;
    }

    @JsonIgnore
    public @Nullable FunctionImpl getNativeCode() {
        return nativeCode;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("parameterTypes", this.getParameterTypes().stream().map(Type::toJson).toList());
        map.put("returnType", this.getReturnType().toJson());
        map.put("code", this.getCode().getStringId());
        map.put("synthetic", this.isSynthetic());
        map.put("name", this.getName());
        map.put("state", this.getState().name());
        map.put("functionType", this.getFunctionType().toJson());
        map.put("native", this.isNative());
        map.put("typeParameters", this.getTypeParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("parameters", this.getParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("type", this.getType().toJson());
        map.put("capturedTypeVariables", this.getCapturedTypeVariables().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("lambdas", this.getLambdas().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("constantPool", this.getConstantPool().getStringId());
        map.put("klasses", this.getKlasses().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("flags", this.getFlags());
        map.put("attributes", this.getAttributes().stream().map(org.metavm.entity.Attribute::toJson).toList());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Function;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.allFlag = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeBoolean(allFlag);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}
