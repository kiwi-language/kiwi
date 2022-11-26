package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.FlowPO;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSummaryDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collection;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("流程")
public class FlowRT extends Entity {

    private String name;
    private final Type type;
    private final ScopeRT rootScope;
    private final Type inputType;
    private final Type outputType;

    private final transient Table<ScopeRT> scopes;
    private final transient Table<NodeRT<?>> nodes;
    private transient long version = 1L;

    public FlowRT(FlowDTO flowDTO, Type inputType, Type outputType, EntityContext context) {
//        super(context);
        this.inputType = inputType;
        this.outputType = outputType;
        setName(flowDTO.name());
        this.scopes = new Table<>();
        this.nodes = new Table<>();
        type = context.getType(flowDTO.typeId());
        rootScope = new ScopeRT(this);
    }

//    FlowRT(FlowPO flowPO, EntityContext context) {
////        super(flowPO.getId(), context);
//        super(flowPO.getId());
//        this.type = context.getType(flowPO.getDeclaringTypeId());
//        this.inputType = context.getType(flowPO.getInputTypeId());
//        this.outputType = context.getType(flowPO.getOutputTypeId());
//        setName(flowPO.getName());
//        this.scopes = new Table<>(context.selectByKey(ScopePO.INDEX_FLOW_ID, id));
//        this.nodes = new Table<>(context.selectByKey(NodePO.INDEX_FLOW_ID, id));
//        this.rootScope = context.getEntity(ScopeRT.class, flowPO.getRootScopeId());
//    }


//    private void initNode(NodePO nodePO, Map<Long, NodePO> map) {
//        if(!map.containsKey(nodePO.getId())) {
//            return;
//        }
//        map.remove(nodePO.getId());
//        if(nodePO.getPrevId() != null) {
//            NodePO prevPO = map.get(nodePO.getPrevId());
//            if(prevPO != null) {
//                initNode(prevPO, map);
//            }
//        }
//        NodeFactory.getFlowNode(nodePO, context);
//    }

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
        return scopes.get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public Table<ScopeRT> getScopes() {
        return scopes;
    }

    @SuppressWarnings("unused")
    public void addScope(ScopeRT scope) {
        this.scopes.add(scope);
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

    public Type getType() {
        return type;
    }

    public Type getInputType() {
        return inputType;
    }

    public Type getOutputType() {
        return outputType;
    }

    public NodeRT<?> getNode(long id) {
        return nodes.get(Entity::getId, id);
    }

    @SuppressWarnings("unused")
    public Collection<NodeRT<?>> getNodes() {
        return nodes;
    }

    void addNode(NodeRT<?> node) {
        nodes.add(node);
        version++;
    }

    void removeNode(NodeRT<?> node) {
        nodes.remove(node);
        version++;
    }

    @Override
    public void remove() {
        scopes.forEach(ScopeRT::remove);
        if(inputType.isAnonymous()) {
            inputType.remove();
        }
        if(outputType.isAnonymous()) {
            outputType.remove();
        }
//        context.remove(this);
    }

    public NodeRT<?> getRootNode() {
        return rootScope.getFirstNode();
    }

    @SuppressWarnings("unused")
    public NodeRT<?> getNodeByNameRequired(String nodeName) {
        return NncUtils.filterOneRequired(nodes, n -> n.getName().equals(nodeName),
                "流程节点'" + nodeName + "'不存在");
    }

    @SuppressWarnings("unused")
    public NodeRT<?> getNodeByName(String nodeName) {
        return NncUtils.find(nodes, n -> n.getName().equals(nodeName));
    }

    public long getVersion() {
        return version;
    }

}
