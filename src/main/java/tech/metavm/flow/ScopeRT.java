package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.flow.rest.ScopeDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@EntityType("流程范围")
public class ScopeRT extends Entity {

    @EntityField("所属流程")
    private final Flow flow;
    @EntityField("所属节点")
    @Nullable
    private final NodeRT<?> owner;
    @ChildEntity("节点列表")
    private final Table<NodeRT<?>> nodes = new Table<>(new TypeReference<>() {
    }, true);
    @ChildEntity("是否未循环体")
    private final boolean withBackEdge;

    public ScopeRT(Flow flow) {
        this(flow, null, false);
    }

    public ScopeRT(Flow flow, @Nullable NodeRT<?> owner) {
        this(flow, owner, false);
    }

    public ScopeRT(Flow flow, @Nullable NodeRT<?> owner, boolean withBackEdge) {
        this.flow = flow;
        this.owner = owner;
        this.withBackEdge = withBackEdge;
    }

    public ScopePO toPO() {
        return new ScopePO(getIdRequired(), ContextUtil.getTenantId(), flow.getId());
    }

    public ScopeDTO toDTO(boolean withNodes) {
        try (var context = SerializeContext.enter()) {
            return new ScopeDTO(
                    context.getTmpId(this), getId(),
                    withNodes ? NncUtils.map(getNodes(), NodeRT::toDTO) : List.of()
            );
        }
    }

    public void addNode(NodeRT<?> node) {
        var pred = node.getPredecessor() != null ? node.getPredecessor() : getLastNode();
        if (pred != null) {
            nodes.addAfter(node, pred);
        } else {
            nodes.add(node);
        }
        flow.addNode(node);
    }

    public NodeRT<?> getPredecessor() {
        return owner;
//        if (withBackEdge) {
//            return owner;
//        }
//        if (owner != null) {
//            return owner.getPredecessor();
//        }
//        return null;
    }

    public @Nullable NodeRT<?> getSuccessor() {
        if (withBackEdge) {
            return owner;
        }
        if (owner != null) {
            return owner.getSuccessor();
        }
        return null;
    }


    public NodeRT<?> getNode(long id) {
        return nodes.get(Entity::getId, id);
    }

    public NodeRT<?> getNode(RefDTO ref) {
        return nodes.get(Entity::getRef, ref);
    }

    public Collection<NodeRT<?>> getNodes() {
        return nodes;
    }

    public NodeRT<?> getNodeById(long id) {
        return nodes.get(Entity::getId, id);
    }

    public NodeRT<?> getNodeByName(String name) {
        return nodes.get(NodeRT::getName, name);
    }

    public NodeRT<?> getNodeByIndex(int index) {
        return nodes.get(index);
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
    public Flow getFlow() {
        return flow;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isWithBackEdge() {
        return withBackEdge;
    }

    @Nullable
    public NodeRT<?> getLastNode() {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }
}
