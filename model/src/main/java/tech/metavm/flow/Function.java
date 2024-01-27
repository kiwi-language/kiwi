package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.rest.FunctionParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("函数")
public class Function extends Flow implements GlobalKey {

    public static final IndexDef<Function> IDX_NAME =
            IndexDef.create(Function.class, "name");

    public static final IndexDef<Function> UNIQUE_IDX_CODE =
            IndexDef.createUnique(Function.class, "code");

    public Function(Long tmpId,
                    String name,
                    @Nullable String code,
                    boolean isNative,
                    boolean isSynthetic,
                    List<Parameter> parameters,
                    Type returnType,
                    List<TypeVariable> typeParameters,
                    List<Type> typeArguments,
                    FunctionType type,
                    @Nullable Function horizontalTemplate,
                    @Nullable CodeSource codeSource,
                    MetadataState state) {
        super(tmpId, name, code, isNative, isSynthetic, parameters, returnType, typeParameters, typeArguments, type, horizontalTemplate, codeSource, state, false);
        checkTypes(parameters, returnType, type);
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
    public FlowExecResult execute(@Nullable ClassInstance self, List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider) {
        NncUtils.requireNull(self);
        checkArguments(arguments);
        if (isNative())
            return NativeFunctions.invoke(this, arguments);
        else
            return new MetaFrame(this.getRootNode(), null, null,
                    arguments, instanceRepository, parameterizedFlowProvider).execute();
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

}
