package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.rest.FunctionParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.MetadataState;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("函数")
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_ALL_FLAG = IndexDef.create(Function.class, "allFlag");

    public static final IndexDef<Function> IDX_NAME =
            IndexDef.create(Function.class, "name");

    public static final IndexDef<Function> UNIQUE_IDX_CODE =
            IndexDef.createUnique(Function.class, "code");

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    public Function(Long tmpId,
                    String name,
                    @Nullable String code,
                    boolean isNative,
                    boolean isSynthetic,
                    List<Parameter> parameters,
                    Type returnType,
                    List<TypeVariable> typeParameters,
                    List<Type> typeArguments,
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
        return getCodeRequired();
    }

    protected String toString0() {
        return getName();
    }

    @Override
    public FlowExecResult execute(@Nullable ClassInstance self, List<Instance> arguments, CallContext callContext) {
        NncUtils.requireNull(self);
        arguments = checkArguments(arguments);
        if (isNative())
            return NativeFunctions.invoke(this, arguments, callContext);
        else
            return new MetaFrame(this.getRootNode(), null, null,
                    arguments, callContext.instanceRepository(),
                    callContext.parameterizedFlowProvider(), callContext.compositeTypeFacade()).execute();
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return getCodeRequired() + "(" +
                NncUtils.join(getParameterTypes(), t -> t.getInternalName(this), ",") + ")";
    }

    @Override
    public Function getParameterized(List<Type> typeArguments) {
        return (Function) super.getParameterized(typeArguments);
    }
}
