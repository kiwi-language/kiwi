package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.flow.rest.ScopeDTO;
import tech.metavm.util.EntityColl;
import tech.metavm.util.NncUtils;

import java.util.*;

public class ScopeRT extends Entity  {

    private transient final EntityColl<NodeRT<?>> nodes = new EntityColl<>();

    private final FlowRT flow;
    private transient NodeRT<?> owner;

    public ScopeRT(FlowRT flow) {
        this(null, flow);
    }

    public ScopeRT(Long id, FlowRT flow) {
        super(id, flow.getContext());
        this.flow = flow;
        flow.addScope(this);
    }

    public void setOwner(NodeRT<?> owner) {
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
        return nodes.get(id);
    }

    public Collection<NodeRT<?>> getNodes() {
        return nodes.values();
    }

    public NodeRT<?> getFirstNode() {
        return NncUtils.filterOne(getNodes(), node -> node.getPredecessor() == null);
    }

    public NodeRT<?> getOwner() {
        return owner;
    }

    public void removeNode(NodeRT<?> node) {
        nodes.remove(node);
        flow.removeNode(node);
    }

    public FlowRT getFlow() {
        return flow;
    }

    public void remove() {
        new ArrayList<>(nodes.values()).forEach(NodeRT::remove);
        context.remove(this);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}
