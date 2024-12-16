package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GlobalKey;
import org.metavm.entity.IndexDef;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.FunctionImpl;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Entity(searchable = true)
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_ALL_FLAG = IndexDef.create(Function.class, "allFlag");

    public static final IndexDef<Function> UNIQUE_NAME =
            IndexDef.createUnique(Function.class, "name");

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    private transient @Nullable FunctionImpl nativeCode;

    public Function(Long tmpId,
                    String name,
                    boolean isNative,
                    boolean isSynthetic,
                    List<NameAndType> parameters,
                    Type returnType,
                    List<TypeVariable> typeParameters,
                    @Nullable CodeSource codeSource,
                    MetadataState state) {
        super(tmpId, name, isNative, isSynthetic, parameters, returnType, typeParameters, codeSource, state);
        resetBody();
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getName();
    }

    protected String toString0() {
        return getName();
    }

    @Override
    public FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext) {
        NncUtils.requireNull(self);
        checkArguments(arguments, flowRef.getTypeMetadata());
        if (isNative()) {
            return Objects.requireNonNull(
                    nativeCode, "Native function " + this + " is not initialized"
            ).run((FunctionRef) flowRef, arguments, callContext);
        }
        else
            return new MetaFrame(callContext.instanceRepository()).execute(
                    getCode(),
                    arguments.toArray(Value[]::new),
                    flowRef.getTypeMetadata(),
                    null
            );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return getName() + "(" +
                NncUtils.join(getParameterTypes(), t -> t.getInternalName(this), ",") + ")";
    }

    @Override
    public FunctionRef getRef() {
        return new FunctionRef(this, List.of());
    }

    public void setNativeCode(FunctionImpl impl) {
        NncUtils.requireTrue(isNative(), "Function " + this + " is not native");
        this.nativeCode = impl;
    }

    public @Nullable FunctionImpl getNativeCode() {
        return nativeCode;
    }


}
