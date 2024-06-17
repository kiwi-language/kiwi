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
                  List<Type> typeArguments,
                  boolean isStatic,
                  @Nullable Method horizontalTemplate,
                  Access access,
                  @Nullable CodeSource codeSource,
                  boolean hidden,
                  MetadataState state) {
        super(tmpId, name, code, isNative, isSynthetic, parameters, returnType, typeParameters, typeArguments, horizontalTemplate, codeSource, state, isAbstract);
        if (isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.declaringType = declaringType;
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
            NncUtils.requireEquals(
                    NncUtils.map(getParameters(), Parameter::getType),
                    overriddenFlow.getParameterTypes()
            );
            NncUtils.requireTrue(overriddenFlow.getReturnType() == getReturnType() ||
                    overriddenFlow.getReturnType().isAssignableFrom(getReturnType()));
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
        return declaringType.getName() + "." + getName();
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
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES);
            if (!overriddenFlow.getReturnType().isAssignableFrom(returnType.accept(subst))) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_RETURN_TYPE_INCORRECT,
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
        return declaringType.isParameterized() ? Objects.requireNonNull(verticalTemplate) : this;
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
    public FlowExecResult execute(@Nullable ClassInstance self, List<? extends Instance> arguments, CallContext callContext) {
        try (var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName() + "." + getName())) {
            if (DebugEnv.debugging) {
                var methodName = getDeclaringType().getName() + "." + getNameWithTypeArguments();
                debugLogger.info("Method.execute: {}", methodName);
                debugLogger.info("Arguments: ");
                arguments.forEach(arg -> debugLogger.info(arg.getText()));
                debugLogger.info(getText());
            }
            if (_static)
                NncUtils.requireNull(self);
            else
                Objects.requireNonNull(self);
            arguments = checkArguments(arguments);
            FlowExecResult result;
            if (isNative())
                result = NativeMethods.invoke(this, self, arguments, callContext);
            else {
                if (!isRootScopePresent())
                    throw new InternalException("fail to invoke method: " + getQualifiedName() + ". root scope not present");
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
                var instance = (ClassInstance) result.ret();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(BuiltinKlasses.runtimeException.get().getType());
                    var exceptionNative = new RuntimeExceptionNative(exception);
                    exceptionNative.RuntimeException(Instances.stringInstance(
                                    "Failed to create object " + instance.getType().getName() + "，" +
                                            "field " + uninitializedField.getName() + " was not initialized"),
                            callContext);
                    return new FlowExecResult(null, exception);
                }
            }
            return result;
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
        return declaringType.getInternalName(null) + "." + getCodeRequired() + "(" +
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
            if (!paramTypes.get(i).isAssignableFrom(thatParamTypes.get(i)))
                return false;
        }
        return true;
    }

    public Method getUltimateTemplate() {
        return getEffectiveVerticalTemplate().getEffectiveHorizontalTemplate().getEffectiveVerticalTemplate();
    }

    @Override
    public MethodRef getRef() {
        return new MethodRef(declaringType.getType(), this.getUltimateTemplate(), getTypeArguments());
    }

    @Override
    public Method getParameterized(List<? extends Type> typeArguments) {
        return (Method) super.getParameterized(typeArguments);
    }

    @Nullable
    @Override
    public Method getExistingParameterized(List<? extends Type> typeArguments) {
        return (Method) super.getExistingParameterized(typeArguments);
    }

    @Override
    protected Flow substitute(SubstitutorV2 substitutor) {
        substitutor.enterElement(declaringType);
        var result = accept(substitutor);
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

}
