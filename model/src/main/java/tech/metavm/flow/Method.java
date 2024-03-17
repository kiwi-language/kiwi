package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeCallContext;
import tech.metavm.entity.natives.NativeMethods;
import tech.metavm.entity.natives.RuntimeExceptionNative;
import tech.metavm.flow.rest.FlowParam;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Method extends Flow implements Property, GenericElement {

    @EntityField("所属类型")
    private final @NotNull ClassType declaringType;
    @EntityField("是否静态")
    private boolean _static;
    @EntityField("可见范围")
    private Access access;
    @ChildEntity("被复写流程")
    private final ReadWriteArray<Method> overridden = addChild(new ReadWriteArray<>(Method.class), "overridden");
    @EntityField("是否构造函数")
    private boolean isConstructor;
    @EntityField("是否抽象")
    private boolean isAbstract;
    /*
     *                       horizontalTemplate
     *  Foo<T>.bar<E>       <------------------      Foo<T>.bar<String>
     *       ^                                            ^
     *       | verticalTemplate                           | verticalTemplate
     *       |                                            |
     * Foo<Integer>.bar<E>  <-------------------  Foo<Integer>.bar<String>
     *                       horizontalTemplate
     */

    @EntityField("垂直模板")
    @CopyIgnore
    @Nullable
    private Method verticalTemplate;
    @EntityField("静态类型")
    @Nullable
    private FunctionType staticType;

    public Method(Long tmpId,
                  @NotNull ClassType declaringType,
                  String name,
                  @Nullable String code,
                  boolean isConstructor,
                  boolean isAbstract,
                  boolean isNative,
                  boolean isSynthetic,
                  List<Parameter> parameters,
                  Type returnType,
                  List<Method> overridden,
                  List<TypeVariable> typeParameters,
                  List<Type> typeArguments,
                  FunctionType type,
                  @Nullable FunctionType staticType,
                  boolean isStatic,
                  @Nullable Method horizontalTemplate,
                  Access access,
                  @Nullable CodeSource codeSource,
                  MetadataState state) {
        super(tmpId, name, code, isNative, isSynthetic, parameters, returnType, typeParameters, typeArguments, type, horizontalTemplate, codeSource, state, isAbstract);
        if (isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.declaringType = declaringType;
        this._static = isStatic;
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.staticType = staticType;
        this.access = access;
        this.overridden.addAll(overridden);
        if (horizontalTemplate == null)
            declaringType.addMethod(this);
        else
            horizontalTemplate.addTemplateInstance(this);
        checkTypes(overridden, parameters, returnType, type, staticType);
    }

    @Nullable
    @Override
    public Flow getTemplate() {
        var template = super.getTemplate();
        if (template != null)
            return template;
        else
            return verticalTemplate;
    }

    @Override
    public void onBind(IEntityContext context) {
        for (Flow overridenFlow : overridden) {
            NncUtils.requireEquals(
                    NncUtils.map(getParameters(), Parameter::getType),
                    overridenFlow.getParameterTypes()
            );
            NncUtils.requireTrue(overridenFlow.getReturnType() == getReturnType() ||
                    overridenFlow.getReturnType().isAssignableFrom(getReturnType()));
        }
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getCodeRequired() + "("
                + NncUtils.join(getParameterTypes(),
                object -> context.getModelName(object, this))
                + ")";
    }

    @Override
    public Method getEffectiveHorizontalTemplate() {
        return (Method) super.getEffectiveHorizontalTemplate();
    }

    @Override
    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    public List<Method> getOverridden() {
        return overridden.toList();
    }

    public List<Method> getAllOverridden() {
        List<Method> allOverridden = NncUtils.listOf(overridden);
        for (Method o : overridden) {
            allOverridden.addAll(o.getAllOverridden());
        }
        return allOverridden;
    }

    @Override
    protected FlowParam getParam(boolean includeCode, SerializeContext serContext) {
        if (includeCode) {
            serContext.writeType(declaringType);
            if (staticType != null)
                serContext.writeType(staticType);
        }
        return new MethodParam(
                isConstructor, isAbstract, _static,
                NncUtils.get(verticalTemplate, serContext::getId),
                serContext.getId(declaringType),
                NncUtils.get(staticType, serContext::getId),
                NncUtils.map(overridden, serContext::getId),
                access.code()
        );
    }

    @Nullable
    public Method getVerticalTemplate() {
        return verticalTemplate;
    }

    @Override
    public @NotNull ClassType getDeclaringType() {
        return declaringType;
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public void setAccess(Access access) {
        this.access = access;
    }

    @Override
    public boolean isStatic() {
        return _static;
    }

    @Override
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        declaringType.rebuildMethodTable();
        return super.beforeRemove(context);
    }

    public boolean isInstanceMethod() {
        return !isStatic();
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void setVerticalTemplate(@Nullable Method verticalTemplate) {
        NncUtils.requireNull(this.verticalTemplate);
        if (verticalTemplate != null)
            NncUtils.requireTrue(verticalTemplate.declaringType == declaringType.getTemplate());
        this.verticalTemplate = verticalTemplate;
    }

    @Override
    public void setCopySource(Object template) {
        NncUtils.requireNull(this.verticalTemplate);
        this.verticalTemplate = (Method) template;
    }

    public void setOverridden(List<Method> overridden) {
        checkTypes(overridden, getParameters(), getReturnType(), getType(), staticType);
        this.overridden.reset(overridden);
        declaringType.rebuildMethodTable();
    }

    public void removeOverridden(Method overridden) {
        this.overridden.remove(overridden);
        declaringType.rebuildMethodTable();
    }

    public void addOverridden(Method overridden) {
        checkTypes(List.of(overridden), getParameters(), getReturnType(), getType(), staticType);
        this.overridden.add(overridden);
        declaringType.rebuildMethodTable();
    }

    public void addOverridden(List<Method> overridden) {
        checkTypes(overridden, getParameters(), getReturnType(), getType(), staticType);
        this.overridden.addAll(overridden);
        declaringType.rebuildMethodTable();
    }

    @Nullable
    @Override
    public Method getHorizontalTemplate() {
        return (Method) super.getHorizontalTemplate();
    }

    public @Nullable FunctionType getStaticType() {
        return staticType;
    }

    public void setStaticType(@Nullable FunctionType staticType) {
        this.staticType = staticType;
    }

    @Override
    protected String toString0() {
        return declaringType.getName() + "." + getName();
    }

    @Override
    protected void checkTypes(List<Parameter> parameters, Type returnType, FunctionType type) {
        checkTypes(overridden, parameters, returnType, type, staticType);
    }

    private void checkTypes(List<Method> overridden, List<Parameter> parameters, Type returnType,
                            FunctionType type, @Nullable FunctionType staticType) {
        super.checkTypes(parameters, returnType, type);
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        for (Flow overriddenFlow : overridden) {
            if (!paramTypes.equals(overriddenFlow.getParameterTypes())) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES);
            }
            if (!overriddenFlow.getReturnType().isAssignableFrom(returnType)) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_RETURN_TYPE_INCORRECT);
            }
        }
        if (isInstanceMethod()) {
            AssertUtils.assertNonNull(staticType, ErrorCode.INSTANCE_METHOD_MISSING_STATIC_TYPE);
            if (!staticType.getParameterTypes().equals(NncUtils.prepend(declaringType, paramTypes))
                    || !staticType.getReturnType().equals(returnType))
                throw new InternalException("Incorrect static function type: " + staticType);
        } else
            NncUtils.requireNull(staticType);
    }

    @Override
    public void update(List<Parameter> parameters, Type returnType, FunctionTypeProvider functionTypeProvider) {
        update(parameters, returnType, overridden, functionTypeProvider);
    }

    public void update(List<Parameter> parameters, Type returnType,
                       @Nullable List<Method> overridden, FunctionTypeProvider functionTypeProvider) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        var type = functionTypeProvider.getFunctionType(paramTypes, returnType);
        var staticType =
                isInstanceMethod() ? functionTypeProvider.getFunctionType(NncUtils.prepend(declaringType, paramTypes), returnType) : null;
        checkTypes(NncUtils.orElse(overridden, this.overridden), parameters, returnType, type, staticType);
        updateInternal(parameters, returnType, type);
        this.staticType = staticType;
        if (overridden != null && overridden != this.overridden)
            this.overridden.reset(overridden);
    }

    public Method getEffectiveVerticalTemplate() {
        return verticalTemplate == null ? this : verticalTemplate;
    }

    public FlowSummaryDTO toSummaryDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new FlowSummaryDTO(
                    serContext.getId(this),
                    getName(),
                    serContext.getId(getDeclaringType()),
                    NncUtils.map(getParameters(), Parameter::toDTO),
                    serContext.getId(getReturnType()),
                    !getParameterTypes().isEmpty(),
                    isConstructor,
                    getState().code()
            );
        }
    }

    @Override
    public FlowExecResult execute(@Nullable ClassInstance self, List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider) {
        try(var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName()+ "." + getName())) {
            if (_static)
                NncUtils.requireNull(self);
            else
                Objects.requireNonNull(self);
            checkArguments(arguments);
            FlowExecResult result;
            if (isNative())
                result = NativeMethods.invoke(this, self, arguments, new NativeCallContext(instanceRepository, parameterizedFlowProvider));
            else
                result = new MetaFrame(this.getRootNode(), declaringType, self,
                        arguments, instanceRepository, parameterizedFlowProvider).execute();
            if (isConstructor && result.ret() != null) {
                var instance = (ClassInstance) result.ret();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(StandardTypes.getRuntimeExceptionType());
                    var exceptionNative = new RuntimeExceptionNative(exception);
                    exceptionNative.RuntimeException(Instances.stringInstance(
                                    "对象" + instance.getType().getName() + "创建失败，" +
                                            "字段" + uninitializedField.getName() + "未初始化"),
                           new NativeCallContext(instanceRepository, parameterizedFlowProvider));
                    return new FlowExecResult(null, exception);
                }
            }
            return result;
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethod(this);
    }

    @Override
    public Object getCopySource() {
        return verticalTemplate;
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if(current == this)
            return "this";
        return declaringType.getInternalName(null) + "." + getCodeRequired() + "(" +
                NncUtils.join(getParameterTypes(), type -> type.getInternalName(this)) + ")";
    }
}
