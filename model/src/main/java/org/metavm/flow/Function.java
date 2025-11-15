package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.entity.natives.FunctionImpl;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(67)
@Entity(searchable = true)
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_ALL_FLAG = IndexDef.create(Function.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));

    public static final IndexDef<Function> UNIQUE_NAME =
            IndexDef.createUnique(Function.class, 1,
                    function -> List.of(Instances.stringInstance(function.getName())));

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    private transient @Nullable FunctionImpl nativeCode;

    public Function(@NotNull Id id,
                    String name,
                    boolean isNative,
                    boolean isSynthetic,
                    List<NameAndType> parameters,
                    int returnTypeIndex,
                    List<TypeVariable> typeParameters,
                    MetadataState state) {
        super(id, name, isNative, isSynthetic, returnTypeIndex, typeParameters, state);
        setParameters(Utils.map(parameters, p -> new Parameter(nextChildId(), p.name(), p.type(), this)));
        resetBody();
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
