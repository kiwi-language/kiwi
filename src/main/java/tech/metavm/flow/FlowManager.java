package tech.metavm.flow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.EntityQuery;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.flow.rest.*;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FlowManager {

    private final InstanceContextFactory contextFactory;

    private final ClassTypeManager classTypeManager;

    private final EntityQueryService entityQueryService;

    public FlowManager(InstanceContextFactory contextFactory, ClassTypeManager classTypeManager, EntityQueryService entityQueryService) {
        this.contextFactory = contextFactory;
        this.classTypeManager = classTypeManager;
        this.entityQueryService = entityQueryService;
    }

    public FlowDTO get(long flowId) {
        IEntityContext context = newContext();
        FlowRT flow = context.getEntity(FlowRT.class, flowId);
        if(flow == null) {
            return null;
        }
        return flow.toDTO();
    }

    @Transactional
    public long create(FlowDTO flowDTO) {
        IEntityContext context = newContext();
        ClassType inputType = saveInputType(null, List.of(), flowDTO.name(), context);
        ClassType outputType = saveOutputType(null, List.of(), flowDTO.name(), context);
        FlowRT flow = new FlowRT(flowDTO, inputType, outputType, context.getClassType(flowDTO.typeId()));
        context.finish();
        NodeRT<?> selfNode = createSelfNode(flow);
        NodeRT<?> inputNode = createInputNode(flow, selfNode);
        createReturnNode(flow, inputNode);
        context.bind(flow);
        return flow.getId();
    }

    private SelfNode createSelfNode(FlowRT flow) {
        NodeDTO selfNodeDTO = NodeDTO.newNode(
                0L,
                "当前记录",
                NodeKind.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO, flow.getRootScope());
    }

    private NodeRT<?> createInputNode(FlowRT flow, NodeRT<?> prev) {
        NodeDTO inputNodeDTO = NodeDTO.newNode(
                0L,
                "流程输入",
                NodeKind.INPUT.code(),
                null
        );
        return new InputNode(inputNodeDTO, prev, flow.getRootScope());
    }

    private void createReturnNode(FlowRT flow, NodeRT<?> prev) {
        NodeDTO returnNodeDTO = NodeDTO.newNode(
                0L,
                "结束",
                NodeKind.RETURN.code(),
                null
        );
        new ReturnNode(returnNodeDTO, prev, flow.getRootScope());
    }

    @Transactional
    public void update(FlowDTO flowDTO) {
        flowDTO.requiredId();
        IEntityContext context = newContext();
        FlowRT flow = context.getEntity(FlowRT.class, flowDTO.id());
        if(flow == null) {
            throw BusinessException.flowNotFound(flowDTO.id());
        }
        flow.update(flowDTO);
        flow.getInputType().setName(getInputTypeName(flow.getName()));
        context.finish();
    }

    @Transactional
    public void delete(long id) {
        IEntityContext context = newContext();
        FlowRT flow = context.getEntity(FlowRT.class, id);
        flow.remove();
        context.finish();
    }

//    public void deleteByOwner(Type owner) {
//        List<FlowRT> flows = owner.getContext().selectByKey(FlowPO.INDEX_DECLARING_TYPE_ID, owner.getId());
//        flows.forEach(FlowRT::remove);
//    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        IEntityContext context = newContext();
        FlowRT flow = context.getEntity(FlowRT.class, nodeDTO.flowId());
        if(flow == null) {
            throw BusinessException.flowNotFound(nodeDTO.flowId());
        }
        ScopeRT scope = context.getEntity(ScopeRT.class, nodeDTO.scopeId());
        nodeDTO = beforeNodeChange(nodeDTO, flow, context);
        NodeRT<?> node = NodeFactory.getFlowNode(nodeDTO, scope);
        context.finish();
        return node.toDTO();
    }

    public NodeDTO getNode(long id) {
        IEntityContext context = newContext();
        NodeRT<?> node = context.getEntity(NodeRT.class, id);
        return NncUtils.get(node, NodeRT::toDTO);
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        nodeDTO.ensureIdSet();
        IEntityContext context = newContext();
        NodeRT<?> node = context.getEntity(NodeRT.class, nodeDTO.id());
        if(node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        nodeDTO = beforeNodeChange(nodeDTO, node.getFlow(), context);
        node.update(nodeDTO);
        context.finish();
        return node.toDTO();
    }

    private NodeDTO beforeNodeChange(NodeDTO nodeDTO, FlowRT flow, IEntityContext context) {
        if(nodeDTO.type() == NodeKind.INPUT.code()) {
            return updateInputType(nodeDTO, flow, context);
        }
        if(nodeDTO.type() == NodeKind.RETURN.code()) {
            return updateOutputType(nodeDTO, flow, context);
        }
        return nodeDTO;
    }

    private NodeDTO updateInputType(NodeDTO nodeDTO, FlowRT flow, IEntityContext context) {
        InputParamDTO inputParam = nodeDTO.getParam();
        long typeId = flow.getInputType().getId();
        List<FieldDTO> fieldDTOs = NncUtils.map(
                inputParam.fields(),
                inputField -> convertToFieldDTO(inputField, flow)
        );
        ClassType inputType = saveInputType(typeId, fieldDTOs, flow.getName(), context);
        context.initIds();
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

    private NodeDTO updateOutputType(NodeDTO nodeDTO, FlowRT flow, IEntityContext context) {
        ReturnParamDTO param = nodeDTO.getParam();
        List<FieldDTO> existingFields = NncUtils.filterAndMap(
                param.fields(),
                f -> f.id() != null,
                f -> convertToFieldDTO(f, flow)
        );
        saveOutputType(flow.getOutputType().getId(), existingFields, flow.getName(), context);
        Map<OutputFieldDTO, Field> newFieldMap = new IdentityHashMap<>();
        for (OutputFieldDTO field : param.fields()) {
            if(field.id() == null) {
                newFieldMap.put(
                        field,
                        classTypeManager.saveField(convertToFieldDTO(field, flow), context)
                );
            }
        }
        context.initIds();
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

    private FieldDTO convertToFieldDTO(Long id, long declaringTypeId, String name, Object defaultValue, long typeId) {
        return FieldDTO.createSimple(
                id,
                name,
                Access.CLASS.code(),
                defaultValue,
                declaringTypeId,
                typeId
        );
    }

    @Transactional
    public void deleteNode(long nodeId) {
        IEntityContext context = newContext();
        NodeRT<?> node = context.getEntity(NodeRT.class, nodeId);
        if(node == null) {
            return;
        }
        node.remove();
        if(node instanceof BranchNode branchNode) {
            branchNode.getBranches().forEach(Branch::remove);
        }
        context.finish();
    }

    private ClassType saveInputType(Long id, List<FieldDTO> fieldDTOs, String flowName, IEntityContext context) {
        TypeDTO typeDTO = TypeDTO.createClass(
                id,
                getInputTypeName(flowName),
                null,
                true,
                true,
                fieldDTOs,
                List.of(),
                "流程输入"
                );
        return classTypeManager.saveTypeWithContent(typeDTO, context);
    }

    private ClassType saveOutputType(Long id, List<FieldDTO> fieldDTOs, String flowName, IEntityContext context) {
        TypeDTO typeDTO = TypeDTO.createClass(
                id,
                getOutputTypeName(flowName),
                null,
                true,
                true,
                fieldDTOs,
                List.of(),
                "流程输出"
        );
        return classTypeManager.saveTypeWithContent(typeDTO, context);
    }

    private String getInputTypeName(String flowName) {
        return "流程输入"  + NncUtils.random();
    }

    private String getOutputTypeName(String flowName) {
        return "流程输出" + NncUtils.random();
    }


    public Page<FlowSummaryDTO> list(long typeId, int page, int pageSize, String searchText) {
        EntityQuery<FlowRT> query = EntityQuery.create(
                FlowRT.class,
                searchText,
                page,
                pageSize
        );
        IEntityContext context = newContext();
        Page<FlowRT> flowPage = entityQueryService.query(query, context);
        return new Page<>(
                NncUtils.map(flowPage.data(), FlowRT::toSummaryDTO),
                flowPage.total()
        );
    }

    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        IEntityContext context = newContext();
        NodeRT<?> nodeRT = context.getEntity(NodeRT.class, branchDTO.ownerId());
        if(nodeRT instanceof BranchNode branchNode) {
            Branch branch = branchNode.addBranch(branchDTO);
            context.finish();
            return branch.toDTO(true, false);
        }
        else {
            throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
        }
    }

    @Transactional
    public BranchDTO updateBranch(BranchDTO branchDTO) {
        NncUtils.requireNonNull(branchDTO.id(), "分支ID必填");
        IEntityContext context = newContext();
        NodeRT<?> owner = context.getEntity(NodeRT.class, branchDTO.ownerId());
        if(owner == null) {
            throw BusinessException.nodeNotFound(branchDTO.ownerId());
        }
        if(owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranch(branchDTO.id());
            if(branch == null) {
                throw BusinessException.branchNotFound(branchDTO.id());
            }
            branch.update(branchDTO);
            context.finish();
            return branch.toDTO(true, false);
        }
        else {
            throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
        }
    }

    @Transactional
    public void deleteBranch(long ownerId, long branchId) {
        IEntityContext context = newContext();
        NodeRT<?> owner = context.getEntity(NodeRT.class, ownerId);
        if(owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranch(branchId);
            if(branch == null) {
                throw BusinessException.branchNotFound(branchId);
            }
            branch.remove();
            context.finish();
        }
        else {
            throw BusinessException.invalidParams("节点" + ownerId + "不是分支节点");
        }
    }

    private IEntityContext newContext() {
        return contextFactory.newContext().getEntityContext();
    }

}
