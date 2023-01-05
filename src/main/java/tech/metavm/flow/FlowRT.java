package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.FlowPO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.Collection;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("流程")
public class FlowRT extends Entity {

    @EntityField("名称")
    private String name;
    @EntityField("所属类型")
    private final ClassType type;
    @EntityField("根流程范围")
    private final ScopeRT rootScope;
    @EntityField("输入类型")
    private final ClassType inputType;
    @EntityField("输出类型")
    private final ClassType outputType;

    private transient Table<ScopeRT> scopes;
    private transient Table<NodeRT<?>> nodes;
    @Nullable
    @EntityField("版本")
    private Long version = 1L;

    public FlowRT(FlowDTO flowDTO, ClassType inputType, ClassType outputType, ClassType declaringType) {
        this.inputType = inputType;
        this.outputType = outputType;
        setName(flowDTO.name());
        this.scopes = new Table<>(ScopeRT.class);
        this.nodes = new Table<>(new TypeReference<>() {});
        type = declaringType;
        rootScope = new ScopeRT(this);
        declaringType.addFlow(this);
    }

    public ScopeRT getRootScope() {
        return rootScope;
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public String getName() {
        return name;
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
        return new FlowDTO(
                id,
                name,
                type.getId(),
                rootScope.toDTO(true),
                getType().toDTO(),
                inputType.getId(),
                outputType.getId()
        );
    }

    public FlowSummaryDTO toSummaryDTO() {
        return new FlowSummaryDTO(
                id,
                name,
                type.getId(),
                NncUtils.get(getInputType(), Entity::getId),
                NncUtils.get(getOutputType(), Entity::getId),
                !getInputType().getFields().isEmpty()
        );
    }

    public FlowPO toPO() {
        return new FlowPO(
                id,
                getTenantId(),
                name,
                type.getId(),
                rootScope.getId(),
                inputType.getId(),
                outputType.getId()
        );
    }

    public void update(FlowDTO flowDTO) {
        setName(flowDTO.name());
    }

    public ClassType getType() {
        return type;
    }

    public ClassType getInputType() {
        return inputType;
    }

    public ClassType getOutputType() {
        return outputType;
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

    @Override
    public void remove() {
        scopes().forEach(ScopeRT::remove);
        if(inputType.isAnonymous()) {
            inputType.remove();
        }
        if(outputType.isAnonymous()) {
            outputType.remove();
        }
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

}
