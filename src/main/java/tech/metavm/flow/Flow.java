package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.persistence.FlowPO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("流程")
public class Flow extends Entity {

    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @ChildEntity("编号")
    private @Nullable String code;
    @ChildEntity("是否构造函数")
    private final boolean isConstructor;
    @ChildEntity("是否抽象")
    private final boolean isAbstract;
    @ChildEntity("是否原生")
    private final boolean isNative;
    @ChildEntity("输入类型")
    @Nullable
    private final ClassType inputType;
    @EntityField("输出类型")
    private final Type outputType;
    @ChildEntity("被复写流程")
    private final @Nullable Flow overridden;
    @ChildEntity("根流程范围")
    private final ScopeRT rootScope;
    private transient Table<ScopeRT> scopes;
    private transient Table<NodeRT<?>> nodes;
    @EntityField("版本")
    private Long version = 1L;

    public Flow(Long tmpId,
                ClassType declaringType,
                String name,
                @Nullable String code,
                boolean isConstructor,
                boolean isAbstract,
                boolean isNative,
                @Nullable ClassType inputType,
                Type outputType,
                @Nullable Flow overridden
                ) {
        super(tmpId);
        if(overridden == null) {
            NncUtils.requireTrue(inputType != null && outputType != null);
        }
        else {
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
        this.nodes = new Table<>(new TypeReference<>() {});
        rootScope = new ScopeRT(this);
        declaringType.addFlow(this);
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

    public FlowDTO toDTO() {
        try(var context = SerializeContext.enter()) {
            return new FlowDTO(
                    context.getTmpId(this),
                    id,
                    getName(),
                    getCode(),
                    isConstructor(),
                    isAbstract,
                    isNative,
                    context.getRef(getDeclaringType()),
                    rootScope.toDTO(true),
                    getDeclaringType().toDTO(true, true),
                    context.getRef(getInputType()),
                    context.getRef(getOutputType()),
                    getInputType().toDTO(),
                    getOutputType().toDTO(),
                    NncUtils.get(getOverridden(), context::getRef)
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
        if(nodes == null) {
            nodes = new Table<>(new TypeReference<>() {});
        }
        return nodes;
    }

    private Table<ScopeRT> scopes() {
        if(scopes == null) {
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
}
