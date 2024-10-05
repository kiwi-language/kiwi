package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.entity.natives.RuntimeExceptionNative;
import org.metavm.flow.rest.FlowParam;
import org.metavm.flow.rest.FlowSummaryDTO;
import org.metavm.flow.rest.MethodParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@EntityType
public class Method extends Flow implements Property, GenericElement {

    public static final IndexDef<Method> IDX_PARAMETERIZED = IndexDef.create(Method.class, "parameterized");
    public static final Logger logger = LoggerFactory.getLogger(Method.class);

    private static final Pattern GETTER_PTN = Pattern.compile("(get|is)([A-Z][a-zA-Z0-9]*)");
    private static final Pattern SETTER_PTN = Pattern.compile("set([A-Z][a-zA-Z0-9]*)");

    private final @NotNull Klass declaringType;
    private boolean _static;
    private Access access;
    @ChildEntity
    private final ReadWriteArray<MethodRef> overridden = addChild(new ReadWriteArray<>(MethodRef.class), "overridden");
    private boolean isConstructor;
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
    @CopyIgnore
    @Nullable
    private Method verticalTemplate;
    @Nullable
    private FunctionType staticType;

    private boolean hidden;

    private final boolean parameterized;

    private transient @Nullable java.lang.reflect.Method javaMethod;

    public Method(Long tmpId,
                  @NotNull Klass declaringType,
                  String name,
                  @Nullable String code,
                  boolean isConstructor,
                  boolean isAbstract,
                  boolean isNative,
                  boolean isSynthetic,
                  List<Parameter> parameters,
                  Type returnType,
                  List<MethodRef> overridden,
                  List<TypeVariable> typeParameters,
                  List<? extends Type> typeArguments,
                  boolean isStatic,
                  @Nullable Method horizontalTemplate,
                  Access access,
                  @Nullable CodeSource codeSource,
                  boolean hidden,
                  MetadataState state) {
        super(tmpId, name, code, isNative, isSynthetic, parameters, returnType, List.of(), List.of(), horizontalTemplate, codeSource, state, isAbstract);
        if (isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.declaringType = declaringType;
        setTypeParameters(typeParameters);
        if(typeParameters.isEmpty())
            setTypeArguments(typeArguments);
        this._static = isStatic;
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        if (!isStatic) {
            this.staticType = new FunctionType(
                    NncUtils.prepend(declaringType.getType(), NncUtils.map(parameters, Parameter::getType)),
                    returnType
            );
        }
        this.access = access;
        this.overridden.addAll(overridden);
        parameterized = horizontalTemplate != null;
        this.hidden = hidden;
        if (horizontalTemplate == null)
            declaringType.addMethod(this);
        checkTypes(overridden, parameters, returnType);
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
        for (var overriddenFlowRef : overridden) {
            var overriddenFlow = overriddenFlowRef.resolve();
            NncUtils.requireEquals(getParameters().size(), overriddenFlow.getParameters().size());
            NncUtils.requireEquals(getTypeParameters().size(), overriddenFlow.getTypeParameters().size());
            var subst = getSubstitutor(overriddenFlow);
            for (int i = 0; i < getParameters().size(); i++) {
                var t1 = getParameter(i).getType();
                var t2 = overriddenFlow.getParameter(i).getType();
                if(!t1.equals(t2.accept(subst))) {
                    throw new IllegalStateException("Method " + getQualifiedName() + " has different parameter types with overridden method " + overriddenFlow.getQualifiedName());
                }
            }
            NncUtils.requireTrue(overriddenFlow.getReturnType().accept(subst).isAssignableFrom(getReturnType()));
        }
    }

    private TypeSubstitutor getSubstitutor(Method overridden) {
        return new TypeSubstitutor(
                NncUtils.map(overridden.getTypeParameters(), TypeVariable::getType),
                NncUtils.map(getTypeParameters(), TypeVariable::getType)
        );
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getCodeNotNull() + "("
                + NncUtils.join(getParameterTypes(),
                t -> t.toExpression(typeDef -> context.getModelName(typeDef, this)))
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
        return NncUtils.map(overridden, MethodRef::resolve);
    }

    public List<MethodRef> getOverriddenRefs() {
        return overridden.toList();
    }

    @Override
    protected FlowParam getParam(boolean includeCode, SerializeContext serContext) {
        if (includeCode) {
            serContext.writeTypeDef(declaringType);
        }
        return new MethodParam(
                isConstructor, isAbstract, _static,
                NncUtils.get(verticalTemplate, serContext::getStringId),
                serContext.getStringId(declaringType),
                NncUtils.get(staticType, t -> t.toExpression(serContext)),
                NncUtils.map(overridden, m -> m.toDTO(serContext)),
                access.code()
        );
    }

    @Nullable
    public Method getVerticalTemplate() {
        return verticalTemplate;
    }

    @Override
    public @NotNull Klass getDeclaringType() {
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
        declaringType.removeErrors(this);
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

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void setCopySource(Object copySource) {
        NncUtils.requireNull(this.verticalTemplate);
        this.verticalTemplate = (Method) copySource;
    }

    public void setOverridden(List<Method> overridden) {
        var overriddenRefs = NncUtils.map(overridden, Method::getRef);
        checkTypes(overriddenRefs, getParameters(), getReturnType());
        this.overridden.reset(overriddenRefs);
        declaringType.rebuildMethodTable();
    }

    public void removeOverridden(Method overridden) {
        this.overridden.removeIf(r -> r.resolve() == overridden);
        declaringType.rebuildMethodTable();
    }

    public void addOverridden(Method overridden) {
        var overriddenRef = overridden.getRef();
        checkTypes(List.of(overriddenRef), getParameters(), getReturnType());
        this.overridden.add(overriddenRef);
        declaringType.rebuildMethodTable();
    }

    public void addOverridden(List<Method> overridden) {
        var overriddenRefs = NncUtils.map(overridden, Method::getRef);
        checkTypes(overriddenRefs, getParameters(), getReturnType());
        this.overridden.addAll(overriddenRefs);
        declaringType.rebuildMethodTable();
    }

    public String getQualifiedSignature() {
        return declaringType.getTypeDesc() + "." + getSignatureString();
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
        this.staticType = NncUtils.get(staticType, t -> addChild(t, "staticType"));
    }

    @Override
    protected String toString0() {
        return getQualifiedSignature();
    }

    private void checkTypes(List<MethodRef> overridden, List<Parameter> parameters, Type returnType) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        for (var overriddenFlowRef : overridden) {
            var overriddenFlow = overriddenFlowRef.resolve();
            if (getTypeParameters().size() != overriddenFlow.getTypeParameters().size()) {
                logger.error("Method {} has an overridden {} with different number of type parameters. {} != {}",
                        getQualifiedName(), overriddenFlow.getQualifiedName(), getTypeParameters().size(), overriddenFlow.getTypeParameters().size());
            }
            var subst = new TypeSubstitutor(NncUtils.map(getTypeParameters(), TypeVariable::getType), NncUtils.map(overriddenFlow.getTypeParameters(), TypeVariable::getType));
            if (!NncUtils.biAllMatch(paramTypes, overriddenFlow.getParameterTypes(), (t1, t2) -> t1.accept(subst).equals(t2)))
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES, getQualifiedSignature());
            if (!overriddenFlow.getReturnType().isAssignableFrom(returnType.accept(subst))) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_RETURN_TYPE_INCORRECT,
                        getQualifiedSignature(), overriddenFlow.getQualifiedSignature(),
                        returnType.accept(subst).getTypeDesc(), overriddenFlow.getReturnType().getTypeDesc());
            }
        }
        if (isInstanceMethod()) {
            AssertUtils.assertNonNull(staticType, ErrorCode.INSTANCE_METHOD_MISSING_STATIC_TYPE);
            if (!staticType.getParameterTypes().equals(NncUtils.prepend(declaringType.getType(), paramTypes))
                    || !staticType.getReturnType().equals(returnType))
                throw new InternalException("Incorrect static function type: " + staticType);
        } else
            NncUtils.requireNull(staticType);
    }

    @Override
    protected void resetType() {
        super.resetType();
        if (!isStatic()) {
            staticType = new FunctionType(
                    NncUtils.prepend(declaringType.getType(), getParameterTypes()),
                    getReturnType()
            );
        }
    }

    public Method getEffectiveVerticalTemplate() {
        return Objects.requireNonNullElse(verticalTemplate, this);
    }

    public FlowSummaryDTO toSummaryDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new FlowSummaryDTO(
                    serContext.getStringId(this),
                    getName(),
                    serContext.getStringId(getDeclaringType()),
                    NncUtils.map(getParameters(), Parameter::toDTO),
                    getReturnType().toExpression(serContext),
                    !getParameterTypes().isEmpty(),
                    isConstructor,
                    getState().code()
            );
        }
    }

    @Override
    public FlowExecResult execute(@Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        try (var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName() + "." + getName())) {
            if (DebugEnv.debugging) {
                var methodName = getDeclaringType().getName() + "." + getNameWithTypeArguments();
                logger.debug("Method.execute: {}", methodName);
                logger.debug("Arguments: ");
                arguments.forEach(arg -> debugLogger.info(arg.getText()));
                logger.debug(getText());
            }
            if (_static)
                NncUtils.requireNull(self);
            else
                Objects.requireNonNull(self);
            arguments = checkArguments(arguments);
            FlowExecResult result;
            if (isNative()) {
                if(javaMethod != null && self != null && self.getMappedEntity() != null)
                    result = invokeNative(self, arguments, callContext);
                else
                    result = NativeMethods.invoke(this, self, arguments, callContext);
            }
            else {
                if (!isRootScopePresent())
                    throw new InternalException("fail to invoke method: " + getQualifiedName() + ". root scope not present");
                if(getRootNode() == null)
                    throw new IllegalStateException("Failed to invoke method " + getQualifiedSignature() + ": empty method body");
                try {
                    result = new MetaFrame(this.getRootNode(), declaringType, self,
                            arguments, callContext.instanceRepository()).execute();
                } catch (Exception e) {
                    logger.info("Fail to execute method {}", getQualifiedName());
                    logger.info(getText());
                    throw new InternalException("fail to execute method " + getQualifiedName(), e);
                }
            }
            if (isConstructor && result.ret() != null) {
                var instance = result.ret().resolveObject();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(StdKlass.runtimeException.get().getType());
                    var exceptionNative = new RuntimeExceptionNative(exception);
                    exceptionNative.RuntimeException(Instances.stringInstance(
                                    "Failed to instantiate " + instance.getType().getTypeDesc() + "，" +
                                            "field " + uninitializedField.getName() + " was not initialized"),
                            callContext);
                    return new FlowExecResult(null, exception);
                }
            }
            return result;
        }
    }

    private @NotNull FlowExecResult invokeNative(Instance self, List<? extends Value> arguments, CallContext callContext) {
        assert javaMethod != null;
        var map = ContextUtil.getEntityContext().getObjectInstanceMap();
        var nativeSelf = Objects.requireNonNull(self.getMappedEntity());
        var nativeArgs = NncUtils.biMap(List.of(javaMethod.getParameterTypes()), arguments, map::getEntity);
        try {
            var r = ReflectionUtils.invoke(nativeSelf, javaMethod, nativeArgs.toArray());
            if (javaMethod.getReturnType() == void.class)
                return FlowExecResult.of(null);
            else
                return FlowExecResult.of(map.getInstance(r));
        }
        catch (Throwable e) {
            var exception = ClassInstance.allocate(StdKlass.runtimeException.type());
            var nat = new RuntimeExceptionNative(exception);
            if(e.getMessage() != null)
                nat.RuntimeException(Instances.stringInstance(e.getMessage()), callContext);
            else
                nat.RuntimeException(callContext);
            return FlowExecResult.ofException(exception);
        }
    }

    @Override
    public String getQualifiedName() {
        return declaringType.getName() + "." + getNameWithTypeArguments();
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
        if (current == this)
            return "this";
        return declaringType.getInternalName(null) + "." + getCodeNotNull() + "(" +
                NncUtils.join(getParameterTypes(), type -> type.getInternalName(this)) + ")";
    }

    public boolean isHiddenBy(Method that) {
        var paramTypes = getParameterTypes();
        var thatParamTypes = that.getParameterTypes();
        if (paramTypes.size() != thatParamTypes.size())
            return false;
        if (paramTypes.equals(thatParamTypes)) {
            if (declaringType.equals(that.getDeclaringType()))
                throw new InternalException(
                        String.format("Methods with the same signature defined in the same type: %s(%s)",
                                getName(), NncUtils.join(paramTypes, Type::getTypeDesc)));
            return declaringType.isAssignableFrom(that.getDeclaringType());
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            var paramType = paramTypes.get(i);
            var thatParamType = thatParamTypes.get(i);
            if (!paramType.isConvertibleFrom(thatParamType))
                return false;
        }
        return true;
    }

    public Method getUltimateTemplate() {
        return getEffectiveVerticalTemplate().getEffectiveHorizontalTemplate().getEffectiveVerticalTemplate();
    }

    @Override
    public MethodRef getRef() {
        return new MethodRef(declaringType.getType(), this.getUltimateTemplate(), isParameterized() ? getTypeArguments() : List.of());
    }

    @Override
    public Method getParameterized(List<? extends Type> typeArguments) {
        return (Method) super.getParameterized(typeArguments);
    }

    @Override
    protected Method createParameterized(List<? extends Type> typeArguments) {
        var parameterized = MethodBuilder
                .newBuilder(declaringType, getName(), getCode())
//                .tmpId(getCopyTmpId(method))
                .horizontalTemplate(this)
                .isSynthetic(isSynthetic())
                .access(getAccess())
                .isStatic(isStatic())
                .typeArguments(typeArguments)
                .build();
        parameterized.setStrictEphemeral(true);
        return parameterized;
    }

    @Nullable
    @Override
    public Method getExistingParameterized(List<? extends Type> typeArguments) {
        return (Method) super.getExistingParameterized(typeArguments);
    }

    @Override
    protected Flow substitute(SubstitutorV2 substitutor) {
        substitutor.enterElement(declaringType);
        var result = substitutor.copy(this);
        substitutor.exitElement();
        return (Flow) result;
    }

    public boolean isGetter() {
        if (!isPublic())
            return false;
        var code = getCode();
        return code != null && GETTER_PTN.matcher(code).matches() && getParameters().isEmpty() && !getReturnType().isVoid();
    }

    public String getPropertyName() {
        var code = Objects.requireNonNull(getCode());
        var matcher = GETTER_PTN.matcher(code);
        if (matcher.matches())
            return NamingUtils.firstCharToLowerCase(matcher.group(2));
        matcher = SETTER_PTN.matcher(code);
        if(matcher.matches())
            return NamingUtils.firstCharToLowerCase(matcher.group(1));
        throw new IllegalStateException("Method " + getQualifiedName() + " is not a getter or setter");
    }

    public boolean isSetter() {
        if (!isPublic())
            return false;
        var code = getCode();
        return code != null && SETTER_PTN.matcher(code).matches() && getParameters().size() == 1;
    }

    @Nullable
    public java.lang.reflect.Method getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(@Nullable java.lang.reflect.Method javaMethod) {
        NncUtils.requireTrue(isNative());
        this.javaMethod = javaMethod;
    }

    @Override
    public String getTypeDesc() {
        return getQualifiedName();
    }
}
