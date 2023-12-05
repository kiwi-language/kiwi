package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSignatureDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType("流程")
public class Flow extends Property implements GenericDeclaration, Callable, GlobalKey {

    @EntityField("是否构造函数")
    private boolean isConstructor;
    @EntityField("是否抽象")
    private boolean isAbstract;
    @EntityField("是否原生")
    private boolean isNative;
    @ChildEntity("参数列表")
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    @EntityField("返回类型")
    private Type returnType;
    @ChildEntity("被复写流程")
    private final ReadWriteArray<Flow> overridden = addChild(new ReadWriteArray<>(Flow.class), "overridden");
    @ChildEntity(value = "根流程范围")
    private ScopeRT rootScope;
    @EntityField("版本")
    private Long version = 1L;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    /*
     *                       horizontalTemplate
     *  Foo<T>.bar<E>       <------------------      Foo<T>.bar<String>
     *       ^                                            ^
     *       | verticalTemplate                           | verticalTemplate
     *       |                                            |
     * Foo<Integer>.bar<E>  <-------------------  Foo<Integer>.bar<String>
     *                       horizontalTemplate
     */
    @Nullable
    @EntityField("垂直模板")
    private final Flow verticalTemplate;
    @Nullable
    @EntityField("水平模板")
    private final Flow horizontalTemplate;
    @ChildEntity("类型实参列表")
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    @ChildEntity("模板实例")
    private final ChildArray<Flow> horizontalInstances = addChild(new ChildArray<>(Flow.class), "horizontalInstances");
    @EntityField("静态类型")
    private FunctionType staticType;

    private transient ReadWriteArray<ScopeRT> scopes;
    private transient ReadWriteArray<NodeRT<?>> nodes;

    public Flow(Long tmpId,
                ClassType declaringType,
                String name,
                @Nullable String code,
                boolean isConstructor,
                boolean isAbstract,
                boolean isNative,
                List<Parameter> parameters,
                Type returnType,
                List<Flow> overridden,
                List<TypeVariable> typeParameters,
                @Nullable Flow verticalTemplate,
                @Nullable Flow horizontalTemplate,
                List<Type> typeArguments,
                FunctionType type,
                FunctionType staticType,
                boolean isStatic,
                MetadataState state
    ) {
        super(tmpId, name, code, type, declaringType, isStatic, state);
        if(isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.isNative = isNative;
        this.returnType = returnType;
        this.overridden.addAll(overridden);
        this.scopes = new ReadWriteArray<>(ScopeRT.class);
        this.nodes = new ReadWriteArray<>(new TypeReference<>() {
        });
        rootScope = addChild(new ScopeRT(this), "rootScope");
        this.verticalTemplate = verticalTemplate;
        this.horizontalTemplate = horizontalTemplate;
        this.typeParameters.addChildren(typeParameters);
        this.typeArguments.addAll(typeArguments);
        this.staticType = staticType;
        setParameters(parameters);
        if (horizontalTemplate != null)
            horizontalTemplate.addTemplateInstance(this);
        else
            declaringType.addFlow(this);
    }

    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return declaringType.getKey(getJavaType) + "."
                + getCodeRequired() + "("
                + NncUtils.join(getParameterTypes(), type -> type.getKey(getJavaType))
                + ")";
    }

    @Override
    public void onBind(IEntityContext context) {
        for (Flow overridenFlow : overridden) {
            NncUtils.requireEquals(
                    NncUtils.map(parameters, Parameter::getType),
                    overridenFlow.getParameterTypes()
            );
            NncUtils.requireTrue(overridenFlow.getReturnType() == returnType ||
                    overridenFlow.getReturnType().isAssignableFrom(returnType));
        }
    }

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public List<Flow> getOverridden() {
        return overridden.toList();
    }

    public List<Flow> getAllOverridden() {
        List<Flow> allOverriden = NncUtils.listOf(overridden);
        for (Flow overridenFlow : overridden) {
            allOverriden.addAll(overridenFlow.getAllOverridden());
        }
        return allOverriden;
    }

//    public ClassType getInputType() {
//        return overridden != null ? overridden.getInputType() : NncUtils.requireNonNull(inputType);
//    }

    public Type getReturnType() {
        return returnType;
    }

    public ScopeRT getRootScope() {
        return rootScope;
    }

    public ScopeRT getScope(long id) {
        return scopes().get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public ReadWriteArray<ScopeRT> getScopes() {
        return scopes();
    }

    @SuppressWarnings("unused")
    public void addScope(ScopeRT scope) {
        this.scopes().add(scope);
    }

    public FlowExecResult execute(@Nullable Instance self, List<Instance> arguments, IInstanceContext context) {
        if(!isStatic())
            Objects.requireNonNull(self);
        checkArguments(arguments);
        if(isNative)
            return NativeInvoker.invoke(this, self, arguments);
        else
            return new MetaFrame(this, self, arguments, context).execute();
    }

    private void checkArguments(List<Instance> arguments) {
        out: if(arguments.size() == parameters.size()) {
            var paramIt = parameters.iterator();
            var argIt = arguments.iterator();
            while (paramIt.hasNext() && argIt.hasNext()) {
                var param = paramIt.next();
                var arg = argIt.next();
                if(!param.getType().isInstance(arg))
                    break out;
            }
            return;
        }
        throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public FlowDTO toDTO(boolean includingCode) {
        try (var context = SerializeContext.enter()) {
            if(includingCode) {
                context.writeType(declaringType);
                getTypeParameters().forEach(context::writeType);
                context.writeType(getType());
                context.writeType(getStaticType());
                context.writeType(returnType);
            }
            var dto =  new FlowDTO(
                    context.getTmpId(this),
                    id,
                    getName(),
                    getCode(),
                    isConstructor(),
                    isAbstract,
                    isNative,
                    context.getRef(getDeclaringType()),
                    includingCode ? rootScope.toDTO(true) : null,
                    context.getRef(getReturnType()),
                    NncUtils.map(parameters, Parameter::toDTO),
                    context.getRef(getType()),
                    context.getRef(staticType),
                    NncUtils.map(typeParameters, context::getRef),
                    NncUtils.get(horizontalTemplate, context::getRef),
                    NncUtils.get(verticalTemplate, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.map(getOverridden(), context::getRef),
                    NncUtils.map(horizontalInstances, ti -> ti.toDTO(false)),
                    isStatic(),
                    getState().code()
            );
            return dto;
        }
    }

    public boolean isError() {
        return getState() == MetadataState.ERROR;
    }

    public void clearNodes() {
        nodes.clear();
    }

    public FlowSummaryDTO toSummaryDTO() {
        try (var context = SerializeContext.enter()) {
            return new FlowSummaryDTO(
                    id,
                    getName(),
                    getDeclaringType().getId(),
                    NncUtils.map(getParameters(), Parameter::toDTO),
                    context.getRef(getReturnType()),
                    !getParameterTypes().isEmpty(),
                    isConstructor,
                    getState().code()
            );
        }
    }

    public InputNode getInputNode() {
        return (InputNode) NncUtils.findRequired(rootScope.getNodes(), node -> node instanceof InputNode);
    }

    private ReadWriteArray<NodeRT<?>> nodes() {
        if (nodes == null) {
            nodes = new ReadWriteArray<>(new TypeReference<>() {
            });
            accept(new VoidStructuralVisitor() {
                @Override
                public Void visitNode(NodeRT<?> node) {
                    nodes.add(node);
                    return super.visitNode(node);
                }
            });
        }
        return nodes;
    }

    public boolean check() {
        return accept(new FlowChecker());
    }

    public void analyze() {
        accept(new FlowAnalyzer());
    }

    private ReadWriteArray<ScopeRT> scopes() {
        if (scopes == null) {
            scopes = new ReadWriteArray<>(ScopeRT.class);
        }
        return scopes;
    }

    public NodeRT<?> getNode(long id) {
        return nodes().get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public ReadonlyArray<NodeRT<?>> getNodes() {
        return nodes();
    }

    void addNode(NodeRT<?> node) {
        nodes().add(node);
        version++;
    }

    void removeNode(NodeRT<?> node) {
        nodes().remove(node);
        version++;
    }

    @Override
    public Flow getTemplate() {
        return getVerticalTemplate();
    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    public boolean isParameterized() {
        return horizontalTemplate != null;
    }

    public Parameter getParameterByCode(String code) {
        return parameters.get(Parameter::getCode, code);
    }

    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    @Override
    public FunctionType getFunctionType() {
        return getType();
    }

    @Override
    public void setFunctionType(FunctionType functionType) {
        setType(functionType);
    }

    public NodeRT<?> getRootNode() {
        return rootScope.tryGetFirstNode();
    }

    @SuppressWarnings("unused")
    public NodeRT<?> getNodeByNameRequired(String nodeName) {
        return NncUtils.filterOneRequired(nodes(), n -> n.getName().equals(nodeName),
                "流程节点'" + nodeName + "'不存在");
    }

    @SuppressWarnings("unused")
    public NodeRT<?> getNodeByName(String nodeName) {
        return NncUtils.find(nodes(), n -> n.getName().equals(nodeName));
    }

    public long getVersion() {
        return version;
    }

    public boolean isNative() {
        return isNative;
    }

    public List<TypeVariable> getTypeParameters() {
        return NncUtils.listOf(typeParameters);
    }

    public ReadonlyArray<? extends Type> getEffectiveTypeArguments() {
        return horizontalTemplate == null ? typeParameters : typeArguments;
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean check) {
        if(check)
            checkTypes(overridden, parameters, returnType, getType(), staticType);
        parameters.forEach(p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
    }

    private void checkTypes(List<Flow> overridden, List<Parameter> parameters, Type returnType,
                            FunctionType type, FunctionType staticType) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        for (Flow overriddenFlow : overridden) {
            if (!paramTypes.equals(overriddenFlow.getParameterTypes())) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES);
            }
            if (!overriddenFlow.getReturnType().isAssignableFrom(returnType)) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_RETURN_TYPE_INCORRECT);
            }
        }
        if(!type.getParameterTypes().equals(paramTypes) || !type.getReturnType().equals(returnType))
            throw new InternalException("Incorrect function type: " + type);
        if(!staticType.getParameterTypes().equals(NncUtils.prepend(declaringType, paramTypes))
                || !staticType.getReturnType().equals(returnType))
            throw new InternalException("Incorrect static function type: " + staticType);
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter);
    }

    public List<Parameter> getParameters() {
        return NncUtils.listOf(parameters);
    }

    public void setRootScope(ScopeRT rootScope) {
        this.rootScope = rootScope;
    }

    @Nullable
    public Flow findHorizontalInstance(List<Type> typeArguments) {
        return NncUtils.find(horizontalInstances, ti -> Objects.equals(ti.getTypeArguments(), typeArguments));
    }

    public List<Flow> getHorizontalInstances() {
        return horizontalInstances.toList();
    }

    public void setReturnType(Type returnType) {
        checkTypes(overridden, parameters.toList(), returnType, getType(), staticType);
        this.returnType = returnType;
    }

    public @Nullable Flow getHorizontalTemplate() {
        return horizontalTemplate;
    }

    public @Nullable Flow getVerticalTemplate() {
        return verticalTemplate;
    }

    public Flow getEffectiveVerticalTemplate() {
        return verticalTemplate == null ? this : verticalTemplate;
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public void update(List<Parameter> parameters, Type returnType,
                       FunctionTypeContext functionTypeContext) {
        update(parameters, returnType, null, functionTypeContext);
    }

    public void update(List<Parameter> parameters, Type returnType,
                       @Nullable List<Flow> overridden, FunctionTypeContext functionTypeContext) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        var type = functionTypeContext.get(paramTypes, returnType);
        var staticType = functionTypeContext.get(NncUtils.prepend(declaringType, paramTypes), returnType);
        checkTypes(NncUtils.orElse(overridden, this.overridden), parameters, returnType, type, staticType);
        setParameters(parameters, false);
        super.setType(type);
        this.staticType = staticType;
        this.returnType = returnType;
        if(overridden != null)
            this.overridden.reset(overridden);
    }

//    public void update(List<Flow> overridden,
//                       List<Parameter> parameters,
//                       Type returnType,
//                       FunctionType type,
//                       FunctionType staticType
//    ) {
//        checkTypes(overridden, parameters, returnType, type, staticType);
//        this.overridden.reset(overridden);
//        this.returnType = returnType;
//        super.setType(type);
//        this.staticType = staticType;
//        setParameters(parameters, false);
//    }

    @Override
    public void setType(Type type) {
        if(type instanceof FunctionType funcType) {
            checkTypes(overridden, parameters.toList(), returnType, funcType, staticType);
            super.setType(type);
        }
        else
            throw new InternalException("Not a function type");
    }

    public void setOverridden(List<Flow> overridden) {
        checkTypes(overridden, parameters.toList(), returnType, getType(), staticType);
        this.overridden.reset(overridden);
    }

    public void removeOverriden(Flow overriden) {
        this.overridden.remove(overriden);
        declaringType.rebuildFlowTable();
    }

    public void addOverriden(Flow overridden) {
        checkTypes(List.of(overridden), parameters.toList(), returnType, getType(), staticType);
        this.overridden.add(overridden);
        declaringType.rebuildFlowTable();
    }

    public void addOverriden(List<Flow> overridden) {
        checkTypes(overridden, parameters.toList(), returnType, getType(), staticType);
        this.overridden.addAll(overridden);
        declaringType.rebuildFlowTable();
    }

    public void addTemplateInstance(Flow templateInstance) {
        horizontalInstances.addChild(templateInstance);
    }

    public void removeTemplateInstance(Flow templateInstance) {
        this.horizontalInstances.remove(templateInstance);
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        this.typeArguments.reset(typeArguments);
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters.resetChildren(typeParameters);
        this.setTypeArguments(typeParameters);
    }

    public FunctionType getStaticType() {
        return staticType;
    }

    public void setStaticType(FunctionType staticType) {
        this.staticType = staticType;
    }

    @Override
    public FunctionType getType() {
        return (FunctionType) super.getType();
    }

    @Override
    protected String toString0() {
        return declaringType.getName() + "." + getName();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFlow(this);
    }

    public FlowSignatureDTO getSignature() {
        return getSignature(getName(), getParameterTypes());
    }

    public static FlowSignatureDTO getSignature(String name, List<Type> parameterTypes) {
        return new FlowSignatureDTO(name,
                NncUtils.map(parameterTypes, Entity::getRef)
        );
    }

}
