package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.flow.rest.*;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class FlowManager {

    @Autowired
    private EntityContextFactory contextFactory;

    @Autowired
    private TypeManager typeManager;

    @Autowired
    private FlowStore flowStore;

    public FlowDTO get(long flowId) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, flowId);
        if(flow == null) {
            return null;
        }
        return flow.toDTO();
    }

    @Transactional
    public long create(FlowDTO flowDTO) {
        EntityContext context = newContext();
        Type inputType = saveInputType(null, List.of(), flowDTO.name(), context);
        FlowRT flow = new FlowRT(flowDTO, inputType, context);
        NodeRT<?> selfNode = createSelfNode(flow);
        createInputNode(flow, selfNode, context);
        context.sync();
        return flow.getId();
    }

    private SelfNode createSelfNode(FlowRT flow) {
        NodeDTO selfNodeDTO = NodeDTO.newNode(
                0L,
                "当前记录",
                NodeType.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO, flow.getRootScope());
    }

    private void createInputNode(FlowRT flow, NodeRT<?> prev, EntityContext context) {
        NodeDTO inputNodeDTO = NodeDTO.newNode(
                0L,
                "流程输入",
                NodeType.INPUT.code(),
                null
        );
        new InputNode(inputNodeDTO, prev, flow.getRootScope());
    }

    @Transactional
    public void update(FlowDTO flowDTO) {
        flowDTO.requiredId();
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, flowDTO.id());
        if(flow == null) {
            throw BusinessException.flowNotFound(flowDTO.id());
        }
        flow.update(flowDTO);
        flow.getInputType().setName(getInputTypeName(flow.getName()));
        context.sync();
    }

    @Transactional
    public void delete(long id) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, id);
        flow.getScopes().forEach(ScopeRT::remove);
        context.remove(flow);
        context.sync();
    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, nodeDTO.flowId());
        if(flow == null) {
            throw BusinessException.flowNotFound(nodeDTO.flowId());
        }
        ScopeRT scope = context.get(ScopeRT.class, nodeDTO.scopeId());
        NodeRT<?> node = NodeFactory.getFlowNode(nodeDTO, scope);
        context.sync();
        return node.toDTO();
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        nodeDTO.ensureIdSet();
        EntityContext context = newContext();
        NodeRT<?> node = context.get(NodeRT.class, nodeDTO.id());
        if(node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        if(node instanceof InputNode inputNode) {
            updateInputType(nodeDTO, inputNode);
        }
        node.update(nodeDTO);
        context.sync();
        return node.toDTO();
    }

    private void updateInputType(NodeDTO nodeDTO, InputNode node) {
        InputParamDTO inputParam = nodeDTO.getParam();
        FlowRT flow = node.getFlow();
        long typeId = flow.getInputType().getId();
        List<FieldDTO> fieldDTOs = NncUtils.map(
                inputParam.fields(),
                inputField -> inputField.toFieldDTO(typeId)
        );
        saveInputType(typeId, fieldDTOs, flow.getName(), flow.getContext());
    }

    @Transactional
    public void removeNode(long nodeId) {
        EntityContext context = newContext();
        NodeRT<?> node = context.get(NodeRT.class, nodeId);
        if(node == null) {
            return;
        }
        node.remove();
        context.sync();
    }

    private Type saveInputType(Long id, List<FieldDTO> fieldDTOs, String flowName, EntityContext context) {
        TypeDTO typeDTO = new TypeDTO(
                id,
                getInputTypeName(flowName),
                TypeCategory.FLOW_INPUT.code(),
                true,
                null,
                "流程输入",
                null,
                fieldDTOs
        );
        return typeManager.saveTypeWithFields(typeDTO, context);
    }

    private String getInputTypeName(String flowName) {
        return flowName + "流程输入";
    }

    public Page<FlowSummaryDTO> list(long typeId, int page, int pageSize, String searchText) {
        FlowQuery query = new FlowQuery(
                ContextUtil.getTenantId(),
                typeId,
                page,
                pageSize,
                searchText
        );
        EntityContext context = newContext();
        Page<FlowRT> flowPage = flowStore.query(query, context);
        return new Page<>(
                NncUtils.map(flowPage.data(), FlowRT::toSummaryDTO),
                flowPage.total()
        );
    }

    @Transactional
    public BranchDTO createBranch(long ownerId, BranchDTO branchDTO) {
        EntityContext context = newContext();
        NodeRT<?> nodeRT = context.get(NodeRT.class, ownerId);
        if(nodeRT instanceof BranchNode branchNode) {
            Branch branch = branchNode.addBranch(branchDTO);
            context.sync();
            return branch.toDTO(true, false);
        }
        else {
            throw BusinessException.invalidParams("节点" + ownerId + "不是分支节点");
        }
    }

    @Transactional
    public BranchDTO updateBranch(long ownerId, BranchDTO branchDTO) {
        NncUtils.require(branchDTO.id(), "分支ID");
        EntityContext context = newContext();
        NodeRT<?> owner = context.get(NodeRT.class, ownerId);
        if(owner == null) {
            throw BusinessException.nodeNotFound(ownerId);
        }
        if(owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranch(branchDTO.id());
            if(branch == null) {
                throw BusinessException.branchNotFound(branchDTO.id());
            }
            branch.update(branchDTO);
            context.sync();
            return branch.toDTO(true, false);
        }
        else {
            throw BusinessException.invalidParams("节点" + ownerId + "不是分支节点");
        }
    }

    @Transactional
    public void deleteBranch(long ownerId, long branchId) {
        EntityContext context = newContext();
        NodeRT<?> owner = context.get(NodeRT.class, ownerId);
        if(owner == null) {
            throw BusinessException.nodeNotFound(ownerId);
        }
        if(owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.deleteBranch(branchId);
            if(branch == null) {
                throw BusinessException.branchNotFound(branchId);
            }
            context.sync();
        }
        else {
            throw BusinessException.invalidParams("节点" + ownerId + "不是分支节点");
        }
    }

    private EntityContext newContext() {
        return contextFactory.newContext();
    }

}
