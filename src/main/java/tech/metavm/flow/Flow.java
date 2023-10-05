package tech.metavm.flow;

import tech.metavm.autograph.Parameter;
import tech.metavm.entity.*;
import tech.metavm.expression.ElementVisitor;
import tech.metavm.flow.persistence.FlowPO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeVariable;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("流程")
public class Flow extends Entity implements GenericDeclaration {

    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @ChildEntity("编号")
    private @Nullable String code;
    @ChildEntity("是否构造函数")
    private boolean isConstructor;
    @ChildEntity("是否抽象")
    private boolean isAbstract;
    @ChildEntity("是否原生")
    private boolean isNative;
    @ChildEntity("输入类型")
    @Nullable
    private final ClassType inputType;
    @EntityField("输出类型")
    private Type outputType;
    @ChildEntity("被复写流程")
    private @Nullable Flow overridden;
    @ChildEntity("根流程范围")
    private ScopeRT rootScope;
    @EntityField("版本")
    private Long version = 1L;
    @ChildEntity("类型参数")
    private final Table<TypeVariable> typeParameters = new Table<>(TypeVariable.class, true);
    @Nullable
    @ChildEntity("模板")
    private final Flow template;
    @ChildEntity("TypeArguments")
    private final Table<Type> typeArguments = new Table<>(Type.class);
    @ChildEntity("模板实例")
    private final Table<Flow> templateInstances = new Table<>(Flow.class, true);
    private transient Table<ScopeRT> scopes;
    private transient Table<NodeRT<?>> nodes;

    public Flow(Long tmpId,
                ClassType declaringType,
                String name,
                @Nullable String code,
                boolean isConstructor,
                boolean isAbstract,
                boolean isNative,
                @Nullable ClassType inputType,
                Type outputType,
                @Nullable Flow overridden,
                List<TypeVariable> typeParameters,
                @Nullable Flow template,
                List<Type> typeArguments
    ) {
        super(tmpId);
        if (overridden == null) {
            NncUtils.requireTrue(inputType != null && outputType != null);
        } else {
            NncUtils.requireTrue(inputType == null);
            NncUtils.requireTrue(overridden.getOutputType().isAssignableFrom(outputType));
        }
        this.declaringType = declaringType;
        this.name = name;
        this.code = code;
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.isNative = isNative;
        this.inputType = inputType;
        this.outputType = outputType;
        this.overridden = overridden;
        this.scopes = new Table<>(ScopeRT.class);
        this.nodes = new Table<>(new TypeReference<>() {
        });
        rootScope = new ScopeRT(this);
        this.template = template;
        this.typeParameters.addAll(typeParameters);
        this.typeArguments.addAll(typeArguments);
        if (template != null) {
            template.addTemplateInstance(this);
        }
        if (template == null || template.getDeclaringType() != declaringType) {
            declaringType.addFlow(this);
        }
    }

    public List<Type> getInputTypes() {
        return NncUtils.map(getInputType().getFields(), Field::getType);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public String getCodeRequired() {
        return NncUtils.requireNonNull(code, "code is set for type " + getName());
    }

    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return declaringType.getCanonicalName(getJavaType) + "."
                + getCodeRequired() + "("
                + NncUtils.join(getParameterTypes(), type -> type.getCanonicalName(getJavaType))
                + ")";
    }

    public List<Type> getParameterTypes() {
        return inputType != null ? NncUtils.map(inputType.getFields(), Field::getType)
                : NncUtils.requireNonNull(overridden).getParameterTypes();
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Nullable
    public Flow getOverridden() {
        return overridden;
    }

    public ClassType getInputType() {
        return overridden != null ? overridden.getInputType() : NncUtils.requireNonNull(inputType);
    }

    public Type getOutputType() {
        return outputType;
    }

    public ScopeRT getRootScope() {
        return rootScope;
    }

    public ScopeRT getScope(long id) {
        return scopes().get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public Table<ScopeRT> getScopes() {
        return scopes();
    }

    @SuppressWarnings("unused")
    public void addScope(ScopeRT scope) {
        this.scopes().add(scope);
    }

    public FlowDTO toDTO(boolean withCode) {
        try (var context = SerializeContext.enter()) {
            return new FlowDTO(
                    context.getTmpId(this),
                    id,
                    getName(),
                    getCode(),
                    isConstructor(),
                    isAbstract,
                    isNative,
                    context.getRef(getDeclaringType()),
                    rootScope.toDTO(withCode),
                    getDeclaringType().toDTO(true, true),
                    context.getRef(getInputType()),
                    context.getRef(getOutputType()),
                    getInputType().toDTO(),
                    getOutputType().toDTO(),
                    NncUtils.map(typeParameters, TypeVariable::toDTO),
                    NncUtils.get(template, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.get(getOverridden(), context::getRef),
                    NncUtils.map(templateInstances, tmpInst -> tmpInst.toDTO(withCode))
            );
        }
    }

    public void clearNodes() {
        nodes.clear();
    }

    public FlowSummaryDTO toSummaryDTO() {
        return new FlowSummaryDTO(
                id,
                getName(),
                getDeclaringType().getId(),
                NncUtils.get(getInputType(), Entity::getId),
                NncUtils.get(getOutputType(), Entity::getId),
                !getInputType().getFields().isEmpty()
        );
    }

    public FlowPO toPO() {
        return new FlowPO(
                id,
                getTenantId(),
                getName(),
                getDeclaringType().getId(),
                rootScope.getId(),
                getInputType().getId(),
                getOutputType().getId()
        );
    }

    public void update(FlowDTO flowDTO) {
        setName(flowDTO.name());
    }

    public InputNode getInputNode() {
        return (InputNode) NncUtils.findRequired(rootScope.getNodes(), node -> node instanceof InputNode);
    }

    private Table<NodeRT<?>> nodes() {
        if (nodes == null) {
            nodes = new Table<>(new TypeReference<>() {});
            new ElementVisitor() {
                @Override
                public void visitNode(NodeRT<?> node) {
                    nodes.add(node);
                }
            }.visitFlow(this);
        }
        return nodes;
    }

    private Table<ScopeRT> scopes() {
        if (scopes == null) {
            scopes = new Table<>(ScopeRT.class);
        }
        return scopes;
    }

    public NodeRT<?> getNode(long id) {
        return nodes().get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public Collection<NodeRT<?>> getNodes() {
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

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public List<Object> beforeRemove() {
        declaringType.removeFlow(this);
        return List.of();
    }

    public boolean isNative() {
        return isNative;
    }

    public List<TypeVariable> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public List<Type> getEffectiveTypeArguments() {
        return template == null ? Collections.unmodifiableList(typeParameters)
                : Collections.unmodifiableList(typeArguments);
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
    }

    public List<Parameter> getParameters() {
        return NncUtils.map(
                inputType.getFields(),
                field -> new Parameter(field.getName(), field.getCode(), field.getType())
        );
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

    public List<Flow> getTemplateInstances() {
        return Collections.unmodifiableList(templateInstances);
    }

    public void setOutputType(Type outputType) {
        this.outputType = outputType;
    }

    @Nullable
    public Flow getTemplate() {
        return template;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
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

    public void setOverridden(@Nullable Flow overridden) {
        this.overridden = overridden;
    }

    public void addTemplateInstance(Flow templateInstance) {
        templateInstances.add(templateInstance);
    }

    public void removeTemplateInstance(Flow templateInstance) {
        this.templateInstances.remove(templateInstance);
    }

    public void setTypeArguments(List<Type> typeArguments) {
        this.typeArguments.clear();
        this.typeArguments.addAll(typeArguments);
    }

    public void setTypeParameters(List<TypeVariable> typeArguments) {
        this.typeParameters.clear();
        this.typeParameters.addAll(typeArguments);
    }

    public Flow getEffectiveTemplate() {
        return template != null ? template : this;
    }

    @Override
    public String toString() {
        return declaringType.getName() + "." + name;
    }
}
