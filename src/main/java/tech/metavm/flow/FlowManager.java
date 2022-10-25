package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.flow.rest.*;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
        Type outputType = saveOutputType(null, List.of(), flowDTO.name(), context);
        FlowRT flow = new FlowRT(flowDTO, inputType, outputType, context);
        NodeRT<?> selfNode = createSelfNode(flow);
        NodeRT<?> inputNode = createInputNode(flow, selfNode);
        createReturnNode(flow, inputNode);
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

    private NodeRT<?> createInputNode(FlowRT flow, NodeRT<?> prev) {
        NodeDTO inputNodeDTO = NodeDTO.newNode(
                0L,
                "流程输入",
                NodeType.INPUT.code(),
                null
        );
        return new InputNode(inputNodeDTO, prev, flow.getRootScope());
    }

    private void createReturnNode(FlowRT flow, NodeRT<?> prev) {
        NodeDTO returnNodeDTO = NodeDTO.newNode(
                0L,
                "结束",
                NodeType.RETURN.code(),
                null
        );
        new ReturnNode(returnNodeDTO, prev, flow.getRootScope());
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
        flow.remove();
        context.sync();
    }

    public void deleteByOwner(Type owner) {
        List<FlowRT> flows = flowStore.getByOwner(owner);
        flows.forEach(FlowRT::remove);
    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, nodeDTO.flowId());
        if(flow == null) {
            throw BusinessException.flowNotFound(nodeDTO.flowId());
        }
        ScopeRT scope = context.get(ScopeRT.class, nodeDTO.scopeId());
        nodeDTO = beforeNodeChange(nodeDTO, flow);
        NodeRT<?> node = NodeFactory.getFlowNode(nodeDTO, scope);
        context.sync();
        return node.toDTO();
    }

    public NodeDTO getNode(long id) {
        EntityContext context = newContext();
        NodeRT<?> node = context.get(NodeRT.class, id);
        return NncUtils.get(node, NodeRT::toDTO);
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        nodeDTO.ensureIdSet();
        EntityContext context = newContext();
        NodeRT<?> node = context.get(NodeRT.class, nodeDTO.id());
        if(node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        nodeDTO = beforeNodeChange(nodeDTO, node.getFlow());
        node.update(nodeDTO);
        context.sync();
        return node.toDTO();
    }

    private NodeDTO beforeNodeChange(NodeDTO nodeDTO, FlowRT flow) {
        if(nodeDTO.type() == NodeType.INPUT.code()) {
            return updateInputType(nodeDTO, flow);
        }
        if(nodeDTO.type() == NodeType.RETURN.code()) {
            return updateOutputType(nodeDTO, flow);
        }
        return nodeDTO;
    }

    private NodeDTO updateInputType(NodeDTO nodeDTO, FlowRT flow) {
        InputParamDTO inputParam = nodeDTO.getParam();
        long typeId = flow.getInputType().getId();
        List<FieldDTO> fieldDTOs = NncUtils.map(
                inputParam.fields(),
                inputField -> convertToFieldDTO(inputField, flow)
        );
        Type inputType = saveInputType(typeId, fieldDTOs, flow.getName(), flow.getContext());
        flow.getContext().sync();
        InputParamDTO newParam = new InputParamDTO(
                inputType.getId(),
                NncUtils.map(inputType.getFields(), f -> new InputFieldDTO(
                        f.getId(),
                        f.getName(),
                        f.getType().getId(),
                        f.getDefaultValue())
                )
        );
        return nodeDTO.copyWithNewParam(newParam);
    }

    private NodeDTO updateOutputType(NodeDTO nodeDTO, FlowRT flow) {
        ReturnParamDTO param = nodeDTO.getParam();
        List<FieldDTO> existingFields = NncUtils.filterAndMap(
                param.fields(),
                f -> f.id() != null,
                f -> convertToFieldDTO(f, flow)
        );
        saveOutputType(flow.getOutputType().getId(), existingFields, flow.getName(), flow.getContext());
        Map<OutputFieldDTO, Field> newFieldMap = new IdentityHashMap<>();
        for (OutputFieldDTO field : param.fields()) {
            if(field.id() == null) {
                newFieldMap.put(
                        field,
                        typeManager.saveField(convertToFieldDTO(field, flow), flow.getContext())
                );
            }
        }
        flow.getContext().sync();
        return nodeDTO.copyWithNewParam(
                new ReturnParamDTO(
                        NncUtils.map(
                                param.fields(),
                                f -> f.id() != null ? f : f.copyWithId(newFieldMap.get(f).getId())
                        )
                )
        );
    }

    private FieldDTO convertToFieldDTO(InputFieldDTO inputFieldDTO, FlowRT flow) {
        return convertToFieldDTO(
                inputFieldDTO.id(),
                flow.getInputType().getId(),
                inputFieldDTO.name(),
                inputFieldDTO.defaultValue(),
                inputFieldDTO.typeId()
        );
    }

    private FieldDTO convertToFieldDTO(OutputFieldDTO inputFieldDTO, FlowRT flow) {
        return convertToFieldDTO(
                inputFieldDTO.id(),
                flow.getOutputType().getId(),
                inputFieldDTO.name(),
                null,
                inputFieldDTO.typeId()
                );
    }

    private FieldDTO convertToFieldDTO(Long id, long ownerId, String name, Object defaultValue, long typeId) {
        return new FieldDTO(
                id,
                name,
//                type.getCategory().code(),
                Access.CLASS.code(),
//                type.isNotNull(),
                defaultValue,
                false,
                false,
//                type.isArray(),
                ownerId,
//                type.getConcreteType().getId(),
//                type.getConcreteType().getName(),
//                List.of(),
                typeId,
                null
        );
    }

    @Transactional
    public void deleteNode(long nodeId) {
        EntityContext context = newContext();
        NodeRT<?> node = context.get(NodeRT.class, nodeId);
        if(node == null) {
            return;
        }
        node.remove();
        if(node instanceof BranchNode branchNode) {
            branchNode.getBranches().forEach(Branch::remove);
        }
        context.sync();
    }

    private Type saveInputType(Long id, List<FieldDTO> fieldDTOs, String flowName, EntityContext context) {
        TypeDTO typeDTO = new TypeDTO(
                id,
                getInputTypeName(flowName),
                TypeCategory.CLASS.code(),
                true,
                true,
                null,
                null,
                "流程输入",
                fieldDTOs,
                List.of()
        );
        return typeManager.saveTypeWithFields(typeDTO, context);
    }

    private Type saveOutputType(Long id, List<FieldDTO> fieldDTOs, String flowName, EntityContext context) {
        TypeDTO typeDTO = new TypeDTO(
                id,
                getOutputTypeName(flowName),
                TypeCategory.CLASS.code(),
                true,
                true,
                null,
                null,
                "流程输出",
//                null,
                fieldDTOs,
                List.of()
        );
        return typeManager.saveTypeWithFields(typeDTO, context);
    }

    private String getInputTypeName(String flowName) {
        return "流程输入"  + NncUtils.random();
    }

    private String getOutputTypeName(String flowName) {
        return "流程输出" + NncUtils.random();
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
        NncUtils.requireNonNull(branchDTO.id(), "分支ID必填");
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
        if(owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranch(branchId);
            if(branch == null) {
                throw BusinessException.branchNotFound(branchId);
            }
            branch.remove();
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
