package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.flow.persistence.FlowPO;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.EntityColl;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;

import java.util.*;

public class FlowRT extends Entity {

    private String name;
    private final Type type;
    private final ScopeRT rootScope;
    private final Type inputType;
    private Long outputTypeId;

    private final transient EntityColl<ScopeRT> scopes = new EntityColl<>();
    private final transient EntityColl<NodeRT<?>> nodes = new EntityColl<>();
    private transient long version = 1L;

    public FlowRT(FlowDTO flowDTO, Type inputType, EntityContext context) {
        super(context);
        this.inputType = inputType;
        setName(flowDTO.name());
        type = context.getType(flowDTO.typeId());
        rootScope = new ScopeRT(this);
    }

    FlowRT(FlowPO flowPO, List<ScopePO> scopePOs, List<NodePO> nodePOs, EntityContext context) {
        super(flowPO.getId(), context);
        this.type = getTypeFromContext(flowPO.getTypeId());
        this.inputType = getTypeFromContext(flowPO.getInputTypeId());
        setName(flowPO.getName());

        if(NncUtils.isNotEmpty(scopePOs)) {
            for (ScopePO scopePO : scopePOs) {
                new ScopeRT(scopePO.getId(), this);
            }
        }

        if(NncUtils.isNotEmpty(nodePOs)) {
            Map<Long, NodePO> map = new HashMap<>(NncUtils.toMap(nodePOs, NodePO::getId));
            for (NodePO nodePO : nodePOs) {
                initNode(nodePO, map);
            }
        }

        this.rootScope = context.get(ScopeRT.class, flowPO.getRootScopeId());
    }


    private void initNode(NodePO nodePO, Map<Long, NodePO> map) {
        if(!map.containsKey(nodePO.getId())) {
            return;
        }
        map.remove(nodePO.getId());
        if(nodePO.getPrevId() != null) {
            NodePO prevPO = map.get(nodePO.getPrevId());
            if(prevPO != null) {
                initNode(prevPO, map);
            }
        }
        NodeFactory.getFlowNode(nodePO, context.get(ScopeRT.class, nodePO.getScopeId()));
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
        return scopes.get(id);
    }

    public EntityColl<ScopeRT> getScopes() {
        return scopes;
    }

    public void addScope(ScopeRT scope) {
        this.scopes.add(scope);
    }

    public FlowDTO toDTO() {
        return new FlowDTO(
                id,
                name,
                type.getId(),
                rootScope.toDTO(true),
                getType().toDTO()
        );
    }

    public FlowSummaryDTO toSummaryDTO() {
        return new FlowSummaryDTO(
                id,
                name,
                type.getId(),
                NncUtils.get(getInputType(), Entity::getId)
        );
    }

    public FlowPO toPO() {
        return new FlowPO(
                id,
                getTenantId(),
                name,
                type.getId(),
                rootScope.getId(),
                inputType.getId()
        );
    }

    public void update(FlowDTO flowDTO) {
        setName(flowDTO.name());
    }

    public Type getType() {
        return type;
    }

    public NodeRT<?> getNodeFromContext(long nodeId) {
        return getFromContext(NodeRT.class, nodeId);
    }

    public FlowRT getFlowFromContext(long flowId) {
        return getFromContext(FlowRT.class, flowId);
    }

    public Type getInputType() {
        return inputType;
    }

    public Type getOutputType() {
        return NncUtils.get(outputTypeId, context::getType);
    }

    public NodeRT<?> getNode(long id) {
        return nodes.get(id);
    }

    public Collection<NodeRT<?>> getNodes() {
        return nodes.values();
    }

    void addNode(NodeRT<?> node) {
        nodes.add(node);
        version++;
    }

    void removeNode(NodeRT<?> node) {
        nodes.remove(node);
        version++;
    }

    public NodeRT<?> getRootNode() {
        return rootScope.getFirstNode();
    }

    public NodeRT<?> getNodeByNameRequired(String nodeName) {
        return NncUtils.filterOneRequired(nodes, n -> n.getName().equals(nodeName),
                "流程节点'" + nodeName + "'不存在");
    }

    public NodeRT<?> getNodeByName(String nodeName) {
        return NncUtils.filterOne(nodes, n -> n.getName().equals(nodeName));
    }

    public long getVersion() {
        return version;
    }
}
