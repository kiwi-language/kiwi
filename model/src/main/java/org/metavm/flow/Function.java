package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.FunctionImpl;
import org.metavm.flow.rest.FunctionParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType(searchable = true)
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_ALL_FLAG = IndexDef.create(Function.class, "allFlag");

    public static final IndexDef<Function> IDX_NAME =
            IndexDef.create(Function.class, "name");

    public static final IndexDef<Function> UNIQUE_IDX_CODE =
            IndexDef.createUnique(Function.class, "code");

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    private transient @Nullable FunctionImpl nativeCode;

    public Function(Long tmpId,
                    String name,
                    @Nullable String code,
                    boolean isNative,
                    boolean isSynthetic,
                    List<Parameter> parameters,
                    Type returnType,
                    List<TypeVariable> typeParameters,
                    List<? extends Type> typeArguments,
                    @Nullable Function horizontalTemplate,
                    @Nullable CodeSource codeSource,
                    MetadataState state) {
        super(tmpId, name, code, isNative, isSynthetic, parameters, returnType, typeParameters, typeArguments, horizontalTemplate, codeSource, state, false);
    }

    @Override
    protected FunctionParam getParam(boolean includeCode, SerializeContext serializeContext) {
        return new FunctionParam();
    }

    @Override
    public boolean isValidGlobalKey() {
        return getCode() != null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getCodeNotNull();
    }

    protected String toString0() {
        return getName();
    }

    @Override
    public FlowExecResult execute(@Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        NncUtils.requireNull(self);
        arguments = checkArguments(arguments);
        if (isNative()) {
            return Objects.requireNonNull(
                    nativeCode, "Native function " + this + " is not initialized"
            ).run(this, arguments, callContext);
        }
        else
            return new MetaFrame(this.getRootNode(), null, null,
                    arguments, callContext.instanceRepository()
            ).execute();
    }

    @Override
    public @Nullable Function getHorizontalTemplate() {
        return (Function) super.getHorizontalTemplate();
    }

    @Override
    public Function getEffectiveHorizontalTemplate() {
        return (Function) super.getEffectiveHorizontalTemplate();
    }

    @Override
    protected Function createParameterized(List<? extends Type> typeArguments) {
        var name = Types.getParameterizedName(getName(), typeArguments);
        var code = Types.getParameterizedCode(getCode(), typeArguments);
        var parameterized = FunctionBuilder
                .newBuilder(name, code)
//                .tmpId(getCopyTmpId(function))
                .horizontalTemplate(this)
                .typeArguments(typeArguments)
                .isSynthetic(isSynthetic())
                .build();
        parameterized.setStrictEphemeral(true);
        return parameterized;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return getCodeNotNull() + "(" +
                NncUtils.join(getParameterTypes(), t -> t.getInternalName(this), ",") + ")";
    }

    @Override
    public FunctionRef getRef() {
        return new FunctionRef(this.getEffectiveHorizontalTemplate(), getTypeArguments());
    }

    @Nullable
    @Override
    public Function getExistingParameterized(List<? extends Type> typeArguments) {
        return (Function) super.getExistingParameterized(typeArguments);
    }

    @Override
    public Function getParameterized(List<? extends Type> typeArguments) {
        return (Function) super.getParameterized(typeArguments);
    }

    public void setNativeCode(FunctionImpl impl) {
        NncUtils.requireTrue(isNative(), "Function " + this + " is not native");
        this.nativeCode = impl;
    }

    public @Nullable FunctionImpl getNativeCode() {
        return nativeCode;
    }
}
