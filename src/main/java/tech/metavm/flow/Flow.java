package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSignatureDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType("流程")
public class Flow extends Property implements GenericDeclaration, Callable {

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
    @ChildEntity("根流程范围")
    private ScopeRT rootScope;
    @EntityField("版本")
    private Long version = 1L;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @Nullable
    @EntityField("模板")
    private final Flow template;
    @ChildEntity("TypeArguments")
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    @ChildEntity("模板实例")
    private final ChildArray<Flow> templateInstances = addChild(new ChildArray<>(Flow.class), "templateInstances");
    @EntityField("静态类型")
    private FunctionType staticType;
    @EntityField("错误")
    private boolean error;

    private transient ReadWriteArray<ScopeRT> scopes;
    private transient ReadWriteArray<NodeRT<?>> nodes;

    public Flow(Long tmpId,
                ClassType declaringType,
                String name,
                @Nullable String code,
                boolean isConstructor,
                boolean isAbstract,
                boolean isNative,
                @Nullable List<Parameter> parameters,
                Type returnType,
                List<Flow> overridden,
                List<TypeVariable> typeParameters,
                @Nullable Flow template,
                List<Type> typeArguments,
                FunctionType type,
                FunctionType staticType
    ) {
        super(tmpId, name, code, type, declaringType);
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.isNative = isNative;
        if (parameters != null) {
            this.parameters.addChildren(parameters);
        }
        this.returnType = returnType;
        this.overridden.addAll(overridden);
        this.scopes = new ReadWriteArray<>(ScopeRT.class);
        this.nodes = new ReadWriteArray<>(new TypeReference<>() {
        });
        rootScope = new ScopeRT(this);
        this.template = template;
        this.typeParameters.addChildren(typeParameters);
        this.typeArguments.addAll(typeArguments);
        this.staticType = staticType;
        if (template != null && template.declaringType == declaringType) {
            template.addTemplateInstance(this);
        }
        if (template == null || template.getDeclaringType() != declaringType) {
            declaringType.addFlow(this);
        }
    }

    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return declaringType.getCanonicalName(getJavaType) + "."
                + getCodeRequired() + "("
                + NncUtils.join(getParameterTypes(), type -> type.getCanonicalName(getJavaType))
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

    public List<Flow> getAllOverriden() {
        List<Flow> allOverriden = NncUtils.listOf(overridden);
        for (Flow overridenFlow : overridden) {
            allOverriden.addAll(overridenFlow.getAllOverriden());
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

    public FlowDTO toDTO(boolean includingCode) {
        try (var context = SerializeContext.enter()) {
            context.writeType(declaringType);
            getTypeParameters().forEach(context::writeType);
            context.writeType(getType());
            context.writeType(getStaticType());
            context.writeType(returnType);
            return new FlowDTO(
                    context.getTmpId(this),
                    id,
                    getName(),
                    getCode(),
                    isConstructor(),
                    isAbstract,
                    isNative,
                    context.getRef(getDeclaringType()),
                    rootScope.toDTO(includingCode || context.isIncludingCode()),
                    context.getRef(getReturnType()),
                    NncUtils.map(parameters, Parameter::toDTO),
                    context.getRef(getType()),
                    context.getRef(staticType),
                    NncUtils.map(typeParameters, context::getRef),
                    NncUtils.get(template, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.map(getOverridden(), context::getRef),
                    NncUtils.map(templateInstances, ti -> ti.toDTO(false)),
                    error
            );
        }
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
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
                    error
            );
        }
    }

    public void update(FlowDTO flowDTO) {
        setName(flowDTO.name());
        isConstructor = flowDTO.isConstructor();
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

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
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
        return rootScope.getFirstNode();
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
        return template == null ? typeParameters : typeArguments;
    }

    public void setParameters(List<Parameter> parameters) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        if (!overridden.isEmpty() && !paramTypes.equals(overridden.get(0).getParameterTypes())) {
            throw new BusinessException(ErrorCode.OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES);
        }
        this.parameters.resetChildren(parameters);
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

    public Flow getTemplateInstance(ClassType declaringType, List<Type> typeArguments) {
        return NncUtils.find(templateInstances, ti ->
                ti.getDeclaringType() == declaringType
                        && Objects.equals(ti.getTypeArguments(), typeArguments)
        );
    }

    public Flow getTemplateInstance(List<Type> typeArguments) {
        return NncUtils.find(templateInstances, ti -> Objects.equals(ti.getTypeArguments(), typeArguments));
    }

    public ReadonlyArray<Flow> getTemplateInstances() {
        return templateInstances;
    }

    public void setReturnType(Type returnType) {
        for (Flow overriddenFlow : overridden) {
            if (!overriddenFlow.getReturnType().isAssignableFrom(returnType)) {
                throw new BusinessException(ErrorCode.OVERRIDE_FLOW_RETURN_TYPE_INCORRECT);
            }
        }
        this.returnType = returnType;
    }

    @Nullable
    public Flow getTemplate() {
        return template;
    }

    @Nullable
    public Flow getRootTemplate() {
        if (template == null) {
            return null;
        }
        var t = template;
        while (t.template != null) {
            t = t.template;
        }
        return t;
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

    public void setOverridden(List<Flow> overridden) {
        this.overridden.clear();
        this.overridden.addAll(overridden);
    }

    public void removeOverriden(Flow overriden) {
        this.overridden.remove(overriden);
        declaringType.rebuildFlowTable();
    }

    public void addOverriden(Flow overridden) {
        this.overridden.add(overridden);
        declaringType.rebuildFlowTable();
    }

    public void addOverriden(List<Flow> overriden) {
        this.overridden.addAll(overriden);
    }

    public void addTemplateInstance(Flow templateInstance) {
        templateInstances.addChild(templateInstance);
    }

    public void removeTemplateInstance(Flow templateInstance) {
        this.templateInstances.remove(templateInstance);
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

    public Flow getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public void setStaticType(FunctionType staticType) {
        this.staticType = staticType;
    }

    @Override
    public FunctionType getType() {
        return (FunctionType) super.getType();
    }

    @Override
    public String toString() {
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
