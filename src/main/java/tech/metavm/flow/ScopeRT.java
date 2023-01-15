package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.flow.rest.ScopeDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@EntityType("流程范围")
public class ScopeRT extends Entity  {

    @EntityField("所属流程")
    private final FlowRT flow;
    @EntityField("所属节点")
    @Nullable
    private final NodeRT<?> owner;
    @ChildEntity("节点列表")
    private final Table<NodeRT<?>> nodes = new Table<>(new TypeReference<>() {}, true);

    public ScopeRT(FlowRT flow) {
        this(flow, null);
    }

    public ScopeRT(FlowRT flow, @Nullable NodeRT<?> owner) {
        this.flow = flow;
        this.owner = owner;
    }

    public ScopePO toPO() {
        return new ScopePO(getId(), flow.getId());
    }

    public ScopeDTO toDTO(boolean withNodes) {
        return new ScopeDTO(
                getId(),
                withNodes ? NncUtils.map(getNodes(), NodeRT::toDTO) : List.of()
        );
    }

    public void addNode(NodeRT<?> node) {
        if(node.getPredecessor() != null) {
            nodes.addAfter(node, node.getPredecessor());
        }
        else {
            if(node.getSuccessor() != null) {
                throw new RuntimeException("New scope root already having successor");
            }
            if(nodes.size() > 0) {
                node.insertAfter(nodes.getFirst());
            }
            nodes.addFirst(node);
        }
        flow.addNode(node);
    }

    public NodeRT<?> getNode(long id) {
        return nodes.get(Entity::getId, id);
    }

    public Collection<NodeRT<?>> getNodes() {
        return nodes;
    }

    public NodeRT<?> getFirstNode() {
        return NncUtils.find(getNodes(), node -> node.getPredecessor() == null);
    }

    public @Nullable NodeRT<?> getOwner() {
        return owner;
    }

    public void removeNode(NodeRT<?> node) {
        nodes.remove(node);
        flow.removeNode(node);
    }

    @JsonIgnore
    public FlowRT getFlow() {
        return flow;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}
